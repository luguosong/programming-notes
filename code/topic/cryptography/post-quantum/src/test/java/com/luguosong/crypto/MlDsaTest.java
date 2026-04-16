package com.luguosong.crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.*;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ML-DSA（Module-Lattice-Based Digital Signature Algorithm）测试
 * <p>
 * ML-DSA 原名 CRYSTALS-Dilithium，是基于格的后量子数字签名算法，
 * 于 2024 年被 NIST 正式标准化为 FIPS 204。
 * <p>
 * BC 1.80 中 ML-DSA 在标准 BouncyCastleProvider（"BC"）中注册，
 * 支持标准 JCA Signature 接口。
 * <p>
 * 安全等级：
 * - ML-DSA-44：NIST 安全等级 2（SHA-256 等价）
 * - ML-DSA-65：NIST 安全等级 3（SHA-384 等价）
 * - ML-DSA-87：NIST 安全等级 5（SHA-512 等价）
 */
@DisplayName("ML-DSA 签名测试")
class MlDsaTest {

    private static final HexFormat HEX = HexFormat.of();

    @BeforeAll
    static void setUp() {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Test
    @DisplayName("ML-DSA-65 完整签名/验签流程")
    void testMlDsa65SignVerify() throws Exception {
        byte[] message = "Hello, Post-Quantum World!".getBytes();

        System.out.println("=== ML-DSA-65 签名/验签流程 ===");
        System.out.println("原始消息: " + new String(message));

        // 1. 生成 ML-DSA-65 密钥对
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("ML-DSA-65", "BC");
        KeyPair keyPair = kpg.generateKeyPair();

        System.out.println("公钥大小: " + keyPair.getPublic().getEncoded().length + " 字节");
        System.out.println("私钥大小: " + keyPair.getPrivate().getEncoded().length + " 字节");

        // 2. 用私钥签名
        Signature signer = Signature.getInstance("ML-DSA-65", "BC");
        signer.initSign(keyPair.getPrivate());
        signer.update(message);
        byte[] signature = signer.sign();

        System.out.println("签名大小: " + signature.length + " 字节");

        // 3. 用公钥验证签名
        Signature verifier = Signature.getInstance("ML-DSA-65", "BC");
        verifier.initVerify(keyPair.getPublic());
        verifier.update(message);
        boolean verified = verifier.verify(signature);

        System.out.println("验签结果: " + (verified ? "通过" : "失败"));
        assertTrue(verified, "合法签名必须验证通过");
    }

    @Test
    @DisplayName("篡改消息后签名验证应失败")
    void testMlDsa65TamperedMessage() throws Exception {
        byte[] originalMessage = "This is the original message.".getBytes();
        byte[] tamperedMessage = "This is the tampered message!".getBytes();

        System.out.println("=== ML-DSA-65 篡改检测 ===");

        // 生成密钥对并签名
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("ML-DSA-65", "BC");
        KeyPair keyPair = kpg.generateKeyPair();

        Signature signer = Signature.getInstance("ML-DSA-65", "BC");
        signer.initSign(keyPair.getPrivate());
        signer.update(originalMessage);
        byte[] signature = signer.sign();

        // 用篡改后的消息验签
        Signature verifier = Signature.getInstance("ML-DSA-65", "BC");
        verifier.initVerify(keyPair.getPublic());
        verifier.update(tamperedMessage);
        boolean verified = verifier.verify(signature);

        System.out.println("原始消息: " + new String(originalMessage));
        System.out.println("篡改消息: " + new String(tamperedMessage));
        System.out.println("验签结果: " + (verified ? "通过" : "失败"));

        assertFalse(verified, "篡改消息后签名验证必须失败");
        System.out.println("验证结果: 篡改被成功检测");
    }

    @Test
    @DisplayName("ML-DSA 参数规格检查")
    void testMlDsaParameterSpecs() throws Exception {
        System.out.println("=== ML-DSA 参数规格检查 ===\n");

        String[] algorithms = {"ML-DSA-44", "ML-DSA-65", "ML-DSA-87"};
        int[] nistLevels = {2, 3, 5};

        System.out.printf("%-14s %-8s %-10s %-10s %-12s%n",
                "算法", "NIST", "公钥(B)", "私钥(B)", "签名(B)");
        System.out.println("-".repeat(58));

        for (int i = 0; i < algorithms.length; i++) {
            String algo = algorithms[i];
            KeyPairGenerator kpg = KeyPairGenerator.getInstance(algo, "BC");
            KeyPair keyPair = kpg.generateKeyPair();

            // 签名
            byte[] testData = "test data for signing".getBytes();
            Signature sig = Signature.getInstance(algo, "BC");
            sig.initSign(keyPair.getPrivate());
            sig.update(testData);
            byte[] signatureBytes = sig.sign();

            int pubKeyLen = keyPair.getPublic().getEncoded().length;
            int privKeyLen = keyPair.getPrivate().getEncoded().length;
            int sigLen = signatureBytes.length;

            System.out.printf("%-14s %-8d %-10d %-10d %-12d%n",
                    algo, nistLevels[i], pubKeyLen, privKeyLen, sigLen);

            assertTrue(pubKeyLen > 0, algo + " 公钥大小应大于 0");
            assertTrue(sigLen > 0, algo + " 签名大小应大于 0");
        }
    }
}
