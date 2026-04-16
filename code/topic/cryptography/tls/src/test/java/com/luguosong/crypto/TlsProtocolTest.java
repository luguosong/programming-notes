package com.luguosong.crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.net.ssl.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TLS 协议握手与证书验证测试
 * <p>
 * 验证完整的 TLS 握手流程（双向通信）和自定义 TrustManager 证书验证。
 */
class TlsProtocolTest {

    @TempDir
    Path tempDir;

    @BeforeAll
    static void setUp() {
        // 注册 BouncyCastle Provider
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * 测试：完整的 TLS 握手流程（服务端 + 客户端双向通信）
     * <p>
     * 使用 @TempDir 临时文件存储证书，验证双向消息收发。
     */
    @Test
    void shouldPerformTLSHandshake() throws Exception {
        // 1. 生成服务端密钥材料
        Object[] serverMaterial = CertificateUtil.generateKeyMaterial("tls-handshake-server");
        KeyPair serverKeyPair = (KeyPair) serverMaterial[0];
        X509Certificate serverCert = (X509Certificate) serverMaterial[1];

        // 2. 将服务端证书导出到临时文件（演示证书文件存储）
        Path certFile = tempDir.resolve("server-cert.cer");
        Files.write(certFile, serverCert.getEncoded());
        assertTrue(Files.exists(certFile), "证书文件应已创建");
        System.out.println("服务端证书已导出: " + certFile);

        // 3. 构建服务端 KeyStore
        KeyStore serverKeyStore = KeyStore.getInstance("PKCS12");
        serverKeyStore.load(null, null);
        serverKeyStore.setKeyEntry("server-key", serverKeyPair.getPrivate(), "changeit".toCharArray(),
                new Certificate[]{serverCert});

        // 4. 构建客户端 TrustStore（信任服务端证书）
        KeyStore clientTrustStore = KeyStore.getInstance("PKCS12");
        clientTrustStore.load(null, null);
        clientTrustStore.setCertificateEntry("server-cert", serverCert);

        // 5. 初始化服务端 SSLContext
        KeyManagerFactory serverKmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        serverKmf.init(serverKeyStore, "changeit".toCharArray());

        TrustManagerFactory serverTmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        serverTmf.init(clientTrustStore); // 服务端信任相同的证书（自签名场景）

        SSLContext serverContext = SSLContext.getInstance("TLS");
        serverContext.init(serverKmf.getKeyManagers(), serverTmf.getTrustManagers(), new SecureRandom());

        // 6. 初始化客户端 SSLContext
        TrustManagerFactory clientTmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        clientTmf.init(clientTrustStore);

        SSLContext clientContext = SSLContext.getInstance("TLS");
        clientContext.init(null, clientTmf.getTrustManagers(), new SecureRandom());

        // 7. 启动服务端线程
        CountDownLatch serverReady = new CountDownLatch(1);
        final int[] portHolder = new int[1];
        final String[] serverReceived = new String[1];
        final boolean[] handshakeSuccess = new boolean[1];

        Thread serverThread = new Thread(() -> {
            try (SSLServerSocket serverSocket = (SSLServerSocket) serverContext
                    .getServerSocketFactory()
                    .createServerSocket(0)) {

                portHolder[0] = serverSocket.getLocalPort();
                serverReady.countDown();

                System.out.println("=== TLS 握手服务端 ===");
                System.out.println("监听端口: " + portHolder[0]);

                try (SSLSocket clientSocket = (SSLSocket) serverSocket.accept()) {
                    // 强制 TLS 握手
                    clientSocket.startHandshake();
                    handshakeSuccess[0] = true;

                    // 打印握手信息
                    SSLSession session = clientSocket.getSession();
                    System.out.println("握手协议: " + session.getProtocol());
                    System.out.println("密码套件: " + session.getCipherSuite());
                    // 单向 TLS 中，服务端不验证客户端证书，因此无法获取对端身份
                    System.out.println("TLS 握手完成");

                    // 读取客户端消息
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(clientSocket.getInputStream()));
                    serverReceived[0] = reader.readLine();
                    System.out.println("收到客户端消息: " + serverReceived[0]);

                    // 发送响应
                    PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
                    writer.println("Handshake OK, Server acknowledges");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        serverThread.start();

        // 8. 等待服务端就绪，然后客户端连接
        serverReady.await();
        Thread.sleep(200);

        // 客户端连接并执行握手
        try (SSLSocket socket = (SSLSocket) clientContext.getSocketFactory()
                .createSocket("localhost", portHolder[0])) {

            System.out.println("=== TLS 握手客户端 ===");

            // 打印握手信息
            socket.startHandshake();
            SSLSession session = socket.getSession();
            System.out.println("握手协议: " + session.getProtocol());
            System.out.println("密码套件: " + session.getCipherSuite());

            // 发送消息
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println("Client Hello via TLS");

            // 读取服务端响应
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String response = reader.readLine();
            System.out.println("服务端响应: " + response);
            assertEquals("Handshake OK, Server acknowledges", response);
        }

        serverThread.join(5000);

        // 验证
        assertTrue(handshakeSuccess[0], "TLS 握手应成功完成");
        assertEquals("Client Hello via TLS", serverReceived[0], "服务端应收到客户端消息");
    }

    /**
     * 测试：通过自定义 X509TrustManager 验证对端证书
     * <p>
     * 使用只信任特定证书的自定义 TrustManager，
     * 验证合法证书通过、非法证书拒绝。
     */
    @Test
    void shouldVerifyPeerCertificate() throws Exception {
        // 1. 生成两个不同的证书
        Object[] trustedMaterial = CertificateUtil.generateKeyMaterial("trusted-server");
        X509Certificate trustedCert = (X509Certificate) trustedMaterial[1];

        Object[] untrustedMaterial = CertificateUtil.generateKeyMaterial("untrusted-server");
        KeyPair untrustedKeyPair = (KeyPair) untrustedMaterial[0];
        X509Certificate untrustedCert = (X509Certificate) untrustedMaterial[1];

        System.out.println("=== 证书信息 ===");
        System.out.println("受信任证书: " + trustedCert.getSubjectX500Principal());
        System.out.println("不受信任证书: " + untrustedCert.getSubjectX500Principal());

        // 2. 构建不受信任证书的服务端 KeyStore
        KeyStore untrustedKeyStore = KeyStore.getInstance("PKCS12");
        untrustedKeyStore.load(null, null);
        untrustedKeyStore.setKeyEntry("untrusted-key", untrustedKeyPair.getPrivate(),
                "changeit".toCharArray(), new Certificate[]{untrustedCert});

        KeyManagerFactory untrustedKmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        untrustedKmf.init(untrustedKeyStore, "changeit".toCharArray());

        // 服务端使用默认 TrustManager（信任所有）
        TrustManagerFactory serverTmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        KeyStore serverTrustStore = KeyStore.getInstance("PKCS12");
        serverTrustStore.load(null, null);
        serverTrustStore.setCertificateEntry("untrusted-cert", untrustedCert);
        serverTmf.init(serverTrustStore);

        SSLContext untrustedServerContext = SSLContext.getInstance("TLS");
        untrustedServerContext.init(untrustedKmf.getKeyManagers(), serverTmf.getTrustManagers(), new SecureRandom());

        // 3. 创建自定义 TrustManager（只信任 trustedCert）
        X509TrustManager customTrustManager = new CustomTrustManager(trustedCert);

        // 4. 客户端 SSLContext（使用自定义 TrustManager）
        SSLContext clientContext = SSLContext.getInstance("TLS");
        clientContext.init(null, new TrustManager[]{customTrustManager}, new SecureRandom());

        // ===== 场景 A：连接不受信任的服务端（应该失败） =====
        System.out.println("\n=== 场景 A：连接不受信任的证书 ===");

        CountDownLatch serverReady = new CountDownLatch(1);
        final int[] portHolder = new int[1];

        Thread untrustedServerThread = new Thread(() -> {
            try (SSLServerSocket serverSocket = (SSLServerSocket) untrustedServerContext
                    .getServerSocketFactory()
                    .createServerSocket(0)) {

                portHolder[0] = serverSocket.getLocalPort();
                serverReady.countDown();

                try (SSLSocket clientSocket = (SSLSocket) serverSocket.accept()) {
                    // 如果连接成功，读取数据
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(clientSocket.getInputStream()));
                    reader.readLine();
                }
            } catch (Exception e) {
                // 服务端可能因客户端断开而异常，属于预期行为
            }
        });
        untrustedServerThread.start();

        serverReady.await();
        Thread.sleep(200);

        // 客户端使用自定义 TrustManager 连接不受信任的服务端
        assertThrows(SSLHandshakeException.class, () -> {
            try (SSLSocket socket = (SSLSocket) clientContext.getSocketFactory()
                    .createSocket("localhost", portHolder[0])) {
                // startHandshake 应抛出 SSLHandshakeException
                // 因为证书不在信任列表中
                socket.startHandshake();
            }
        }, "连接不受信任证书时应该抛出 SSLHandshakeException");

        System.out.println("已验证：不受信任证书被正确拒绝");

        untrustedServerThread.join(3000);

        // ===== 场景 B：连接受信任的服务端（应该成功） =====
        System.out.println("\n=== 场景 B：连接受信任的证书 ===");

        // 构建受信任证书的服务端
        KeyStore trustedKeyStore = KeyStore.getInstance("PKCS12");
        trustedKeyStore.load(null, null);
        KeyPair trustedKeyPair = (KeyPair) trustedMaterial[0];
        trustedKeyStore.setKeyEntry("trusted-key", trustedKeyPair.getPrivate(),
                "changeit".toCharArray(), new Certificate[]{trustedCert});

        KeyManagerFactory trustedKmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        trustedKmf.init(trustedKeyStore, "changeit".toCharArray());

        KeyStore trustedServerTrustStore = KeyStore.getInstance("PKCS12");
        trustedServerTrustStore.load(null, null);
        trustedServerTrustStore.setCertificateEntry("trusted-cert", trustedCert);

        TrustManagerFactory trustedServerTmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustedServerTmf.init(trustedServerTrustStore);

        SSLContext trustedServerContext = SSLContext.getInstance("TLS");
        trustedServerContext.init(trustedKmf.getKeyManagers(), trustedServerTmf.getTrustManagers(), new SecureRandom());

        CountDownLatch trustedServerReady = new CountDownLatch(1);
        final int[] trustedPortHolder = new int[1];
        final boolean[] trustedHandshakeOk = new boolean[1];

        Thread trustedServerThread = new Thread(() -> {
            try (SSLServerSocket serverSocket = (SSLServerSocket) trustedServerContext
                    .getServerSocketFactory()
                    .createServerSocket(0)) {

                trustedPortHolder[0] = serverSocket.getLocalPort();
                trustedServerReady.countDown();

                try (SSLSocket clientSocket = (SSLSocket) serverSocket.accept()) {
                    clientSocket.startHandshake();
                    trustedHandshakeOk[0] = true;

                    // 读取并响应
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(clientSocket.getInputStream()));
                    reader.readLine();

                    PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
                    writer.println("Certificate verification passed");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        trustedServerThread.start();

        trustedServerReady.await();
        Thread.sleep(200);

        // 客户端连接受信任的服务端
        try (SSLSocket socket = (SSLSocket) clientContext.getSocketFactory()
                .createSocket("localhost", trustedPortHolder[0])) {

            socket.startHandshake();
            SSLSession session = socket.getSession();
            System.out.println("握手协议: " + session.getProtocol());
            System.out.println("密码套件: " + session.getCipherSuite());

            // 发送消息
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println("Trusted connection test");

            // 读取服务端响应
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String response = reader.readLine();
            assertEquals("Certificate verification passed", response);
        }

        trustedServerThread.join(5000);

        assertTrue(trustedHandshakeOk[0], "受信任证书的握手应成功完成");
        System.out.println("已验证：受信任证书通过验证");
    }

    /**
     * 自定义 TrustManager 实现
     * <p>
     * 只信任特定证书，用于测试证书验证逻辑。
     * 当对端证书与预置的可信证书不匹配时，抛出 CertificateException。
     */
    static class CustomTrustManager implements X509TrustManager {

        private final X509Certificate trustedCert;

        CustomTrustManager(X509Certificate trustedCert) {
            this.trustedCert = trustedCert;
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            // 信任任意客户端证书（本测试中不做客户端证书验证）
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            if (chain == null || chain.length == 0) {
                throw new CertificateException("证书链为空");
            }

            // 验证服务端证书是否与受信任证书匹配
            X509Certificate serverCert = chain[0];
            if (!trustedCert.equals(serverCert)) {
                // 通过公钥比对（Bouncy Castle 生成的证书实例可能不同）
                boolean publicKeyMatch = trustedCert.getPublicKey().equals(serverCert.getPublicKey());
                if (!publicKeyMatch) {
                    System.out.println("证书验证失败 - 预期: " + trustedCert.getSubjectX500Principal()
                            + ", 实际: " + serverCert.getSubjectX500Principal());
                    throw new CertificateException("服务端证书不在信任列表中: "
                            + serverCert.getSubjectX500Principal());
                }
            }

            System.out.println("证书验证通过: " + chain[0].getSubjectX500Principal());
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            // 返回受信任的证书
            return new X509Certificate[]{trustedCert};
        }
    }
}
