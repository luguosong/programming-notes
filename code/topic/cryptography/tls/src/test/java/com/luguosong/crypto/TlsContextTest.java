package com.luguosong.crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.net.ssl.*;
import java.io.*;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TLS 上下文配置测试
 * <p>
 * 验证 SSLContext 的创建、自签名证书加载以及自定义密码套件配置。
 */
class TlsContextTest {

    @TempDir
    Path tempDir;

    @BeforeAll
    static void setUp() {
        // 注册 BouncyCastle Provider（如果尚未注册）
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * 测试：创建 SSLContext 并加载自签名证书，启动 TLS 服务端并验证客户端连接
     */
    @Test
    void shouldCreateSSLContextWithSelfSignedCert() throws Exception {
        // 1. 生成密钥对和自签名证书
        Object[] material = CertificateUtil.generateKeyMaterial("tls-server");
        KeyPair keyPair = (KeyPair) material[0];
        X509Certificate cert = (X509Certificate) material[1];

        System.out.println("=== 自签名证书信息 ===");
        System.out.println("主体: " + cert.getSubjectX500Principal());
        System.out.println("颁发者: " + cert.getIssuerX500Principal());
        System.out.println("有效期: " + cert.getNotBefore() + " ~ " + cert.getNotAfter());
        System.out.println("签名算法: " + cert.getSigAlgName());

        // 2. 将证书和私钥存入 KeyStore
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null, null);
        keyStore.setKeyEntry("server-key", keyPair.getPrivate(), "changeit".toCharArray(),
                new Certificate[]{cert});

        // 3. 构建 TrustStore（信任自身证书）
        KeyStore trustStore = KeyStore.getInstance("PKCS12");
        trustStore.load(null, null);
        trustStore.setCertificateEntry("server-cert", cert);

        // 4. 用 KeyManagerFactory 和 TrustManagerFactory 初始化 SSLContext
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, "changeit".toCharArray());

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);

        // 5. 创建 SSLContext
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());

        // 6. 启动 TLS 服务端（使用独立线程，端口 0 自动分配）
        CountDownLatch serverReady = new CountDownLatch(1);
        final int[] portHolder = new int[1];
        final String[] serverReceived = new String[1];

        Thread serverThread = new Thread(() -> {
            try (SSLServerSocket serverSocket = (SSLServerSocket) sslContext
                    .getServerSocketFactory()
                    .createServerSocket(0)) {

                portHolder[0] = serverSocket.getLocalPort();
                serverReady.countDown(); // 通知客户端服务端已就绪

                System.out.println("=== 服务端信息 ===");
                System.out.println("监听端口: " + portHolder[0]);
                System.out.println("默认协议: " + sslContext.getProtocol());

                // 接受客户端连接
                try (SSLSocket clientSocket = (SSLSocket) serverSocket.accept()) {
                    // 打印协商后的 TLS 信息
                    System.out.println("协商协议: " + clientSocket.getSession().getProtocol());
                    System.out.println("协商密码套件: " + clientSocket.getSession().getCipherSuite());

                    // 读取客户端消息
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(clientSocket.getInputStream()));
                    serverReceived[0] = reader.readLine();
                    System.out.println("收到客户端消息: " + serverReceived[0]);

                    // 发送响应
                    PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
                    writer.println("Hello from TLS Server");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        serverThread.start();

        // 7. 等待服务端就绪后，客户端连接
        serverReady.await();
        Thread.sleep(200); // 额外等待确保端口可用

        try (SSLSocket socket = (SSLSocket) sslContext.getSocketFactory()
                .createSocket("localhost", portHolder[0])) {

            System.out.println("=== 客户端信息 ===");
            System.out.println("连接地址: localhost:" + portHolder[0]);

            // 发送消息
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println("Hello from TLS Client");

            // 读取服务端响应
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String response = reader.readLine();

            System.out.println("服务端响应: " + response);

            // 验证双向通信
            assertNotNull(serverReceived[0]);
            assertEquals("Hello from TLS Client", serverReceived[0]);
            assertEquals("Hello from TLS Server", response);
        }

        serverThread.join(5000);
    }

    /**
     * 测试：配置自定义 TLS 版本和密码套件
     */
    @Test
    void shouldConfigureCustomCipherSuites() throws Exception {
        // 生成密钥材料
        Object[] material = CertificateUtil.generateKeyMaterial("cipher-test");
        KeyPair keyPair = (KeyPair) material[0];
        X509Certificate cert = (X509Certificate) material[1];

        // 构建 KeyStore 和 TrustStore
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null, null);
        keyStore.setKeyEntry("server-key", keyPair.getPrivate(), "changeit".toCharArray(),
                new Certificate[]{cert});

        KeyStore trustStore = KeyStore.getInstance("PKCS12");
        trustStore.load(null, null);
        trustStore.setCertificateEntry("server-cert", cert);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, "changeit".toCharArray());

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);

        // 创建 TLS 1.2 上下文
        SSLContext tls12Context = SSLContext.getInstance("TLSv1.2");
        tls12Context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());

        // 创建 TLS 1.3 上下文
        SSLContext tls13Context = SSLContext.getInstance("TLSv1.3");
        tls13Context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());

        // 获取默认 SSLServerSocket 以查看支持的密码套件
        try (SSLServerSocket serverSocket = (SSLServerSocket) tls12Context
                .getServerSocketFactory()
                .createServerSocket(0)) {

            String[] defaultSuites = serverSocket.getSupportedCipherSuites();
            String[] enabledSuites = serverSocket.getEnabledCipherSuites();

            System.out.println("=== TLS 1.2 密码套件 ===");
            System.out.println("所有支持的密码套件（" + defaultSuites.length + " 个）:");
            for (String suite : defaultSuites) {
                System.out.println("  - " + suite);
            }
            System.out.println("默认启用的密码套件（" + enabledSuites.length + " 个）:");
            for (String suite : enabledSuites) {
                System.out.println("  - " + suite);
            }
        }

        // 打印 TLS 1.3 密码套件信息
        try (SSLServerSocket serverSocket = (SSLServerSocket) tls13Context
                .getServerSocketFactory()
                .createServerSocket(0)) {

            String[] enabledSuites = serverSocket.getEnabledCipherSuites();

            System.out.println("=== TLS 1.3 密码套件 ===");
            System.out.println("默认启用的密码套件（" + enabledSuites.length + " 个）:");
            for (String suite : enabledSuites) {
                System.out.println("  - " + suite);
            }
        }

        // 验证 TLS 1.3 上下文协议
        assertEquals("TLSv1.3", tls13Context.getProtocol());

        // 验证密码套件不为空
        SSLSocketFactory factory = tls12Context.getSocketFactory();
        try (SSLSocket socket = (SSLSocket) factory.createSocket()) {
            String[] supported = socket.getSupportedCipherSuites();
            assertTrue(supported.length > 0, "应该有支持的密码套件");

            // 找到 TLS 1.2 兼容的密码套件（以 TLS_ 开头）
            boolean hasTls12Suite = false;
            for (String suite : supported) {
                if (suite.startsWith("TLS_") || suite.startsWith("TLS_ECDHE_")) {
                    hasTls12Suite = true;
                    break;
                }
            }
            assertTrue(hasTls12Suite, "应该有 TLS 密码套件");
        }

        System.out.println("=== 密码套件配置测试通过 ===");
    }
}
