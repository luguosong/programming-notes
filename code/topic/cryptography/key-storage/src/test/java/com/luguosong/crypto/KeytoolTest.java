package com.luguosong.crypto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.cert.Certificate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * keytool 命令行工具演示
 *
 * keytool 是 JDK 自带的密钥和证书管理工具，
 * 可通过 ProcessBuilder 在 Java 程序中调用。
 */
class KeytoolTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldCreateKeyStoreViaKeytool() throws Exception {
        File ksFile = tempDir.resolve("keytool-keystore.p12").toFile();

        // ===== 第一步：通过 keytool 命令创建密钥库 =====
        // keytool -genkeypair：生成密钥对并存入密钥库
        ProcessBuilder pb = new ProcessBuilder(
                "keytool", "-genkeypair",
                "-alias", "test-key",
                "-keyalg", "RSA",
                "-keysize", "2048",
                "-dname", "CN=Keytool Test, O=Luguosong, C=CN",
                "-validity", "365",
                "-keystore", ksFile.getAbsolutePath(),
                "-storepass", "changeit",
                "-storetype", "PKCS12"
        );
        pb.redirectErrorStream(true);

        Process process = pb.start();
        // 读取命令输出
        String output = new String(process.getInputStream().readAllBytes());
        int exitCode = process.waitFor();

        assertEquals(0, exitCode, "keytool 命令应执行成功，输出: " + output);
        System.out.println("keytool 输出:\n" + output);
        System.out.println("密钥库文件已创建: " + ksFile.getAbsolutePath());

        // ===== 第二步：使用 KeyStore API 加载并验证 =====
        KeyStore ks = KeyStore.getInstance("PKCS12");
        try (FileInputStream fis = new FileInputStream(ksFile)) {
            ks.load(fis, "changeit".toCharArray());
        }

        // 验证密钥条目存在
        assertTrue(ks.containsAlias("test-key"), "别名 test-key 应该存在");
        assertTrue(ks.isKeyEntry("test-key"), "test-key 应为密钥条目");

        // 验证证书
        Certificate cert = ks.getCertificate("test-key");
        assertNotNull(cert, "证书应该存在");
        assertEquals("X.509", cert.getType());
        System.out.println("证书类型: " + cert.getType());
        System.out.println("证书字符串: " + cert.toString().lines().findFirst().orElse(""));

        // 验证私钥可读
        java.security.Key privateKey = ks.getKey("test-key", "changeit".toCharArray());
        assertNotNull(privateKey, "私钥应该可读取");
        assertEquals("RSA", privateKey.getAlgorithm());
        System.out.println("私钥算法: " + privateKey.getAlgorithm());

        System.out.println("keytool 创建的密钥库验证通过");
    }
}
