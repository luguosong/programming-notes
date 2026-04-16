package com.luguosong.crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.*;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RSA 数字签名测试：PKCS#1 v1.5 和 RSA-PSS
 */
class RsaSignatureTest {

    @BeforeAll
    static void setUp() {
        // 注册 BouncyCastle Provider
        Security.addProvider(new BouncyCastleProvider());
    }

    @Test
    @DisplayName("RSA PKCS#1 v1.5 签名与验证 - SHA256withRSA（2048 位）")
    void shouldSignAndVerifyWithRSA() throws Exception {
        // 1. 生成 RSA 2048 位密钥对
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        // 2. 准备待签名的原始数据
        String originalMessage = "RSA PKCS#1 v1.5 签名测试消息";
        byte[] messageBytes = originalMessage.getBytes();

        // 3. 用私钥签名（PKCS#1 v1.5 填充方案）
        Signature signature = Signature.getInstance("SHA256withRSA", "BC");
        signature.initSign(keyPair.getPrivate());
        signature.update(messageBytes);
        byte[] signatureBytes = signature.sign();

        System.out.println("RSA PKCS#1 v1.5 签名长度: " + signatureBytes.length + " 字节");
        // RSA 2048 位密钥的签名长度应为 256 字节（2048 / 8）
        assertEquals(256, signatureBytes.length, "RSA 2048 位密钥的签名长度应为 256 字节");

        // 4. 用公钥验证签名
        signature.initVerify(keyPair.getPublic());
        signature.update(messageBytes);
        assertTrue(signature.verify(signatureBytes), "签名验证应该通过");

        // 5. 验证：篡改消息后签名应该无法通过
        byte[] tamperedBytes = "被篡改的消息".getBytes();
        signature.initVerify(keyPair.getPublic());
        signature.update(tamperedBytes);
        assertFalse(signature.verify(signatureBytes), "篡改消息后签名验证应该失败");

        // 6. 验证：不同消息产生的签名不同
        Signature sig2 = Signature.getInstance("SHA256withRSA", "BC");
        sig2.initSign(keyPair.getPrivate());
        sig2.update("另一条消息".getBytes());
        byte[] anotherSignatureBytes = sig2.sign();
        assertNotEquals(
                java.util.Base64.getEncoder().encodeToString(signatureBytes),
                java.util.Base64.getEncoder().encodeToString(anotherSignatureBytes),
                "不同消息的签名应该不同"
        );
    }

    @Test
    @DisplayName("RSA-PSS 签名与验证 - SHA256withRSAandMGF1")
    void shouldSignAndVerifyWithRSAPSS() throws Exception {
        // 1. 生成 RSA 2048 位密钥对
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        // 2. 准备待签名的原始数据
        String originalMessage = "RSA-PSS 签名测试消息";
        byte[] messageBytes = originalMessage.getBytes();

        // 3. 配置 PSS 参数
        //    - 消息摘要算法：SHA-256
        //    - MGF（掩码生成函数）：MGF1，使用 SHA-256
        //    - 盐值长度：32 字节
        //    - 尾部字段：1（固定值，表示 Trailer Field）
        PSSParameterSpec pssParameterSpec = new PSSParameterSpec(
                "SHA-256",                          // 消息摘要算法
                "MGF1",                              // MGF 算法
                new MGF1ParameterSpec("SHA-256"),    // MGF 参数
                32,                                  // 盐值长度（字节）
                1                                    // Trailer Field（固定为 1）
        );

        // 4. 用私钥签名（RSA-PSS 填充方案）
        Signature signature = Signature.getInstance("SHA256withRSAandMGF1", "BC");
        signature.setParameter(pssParameterSpec);
        signature.initSign(keyPair.getPrivate());
        signature.update(messageBytes);
        byte[] signatureBytes = signature.sign();

        System.out.println("RSA-PSS 签名长度: " + signatureBytes.length + " 字节");
        System.out.println("RSA-PSS 盐值长度: " + pssParameterSpec.getSaltLength() + " 字节");
        // RSA 2048 位密钥的签名长度同样为 256 字节
        assertEquals(256, signatureBytes.length, "RSA 2048 位密钥的 PSS 签名长度应为 256 字节");

        // 5. 用公钥验证签名（必须使用相同的 PSS 参数）
        signature.setParameter(pssParameterSpec);
        signature.initVerify(keyPair.getPublic());
        signature.update(messageBytes);
        assertTrue(signature.verify(signatureBytes), "签名验证应该通过");

        // 6. 验证：篡改消息后签名应该无法通过
        byte[] tamperedBytes = "被篡改的消息".getBytes();
        signature.setParameter(pssParameterSpec);
        signature.initVerify(keyPair.getPublic());
        signature.update(tamperedBytes);
        assertFalse(signature.verify(signatureBytes), "篡改消息后签名验证应该失败");

        // 7. 验证：不同消息产生的签名不同
        Signature sig2 = Signature.getInstance("SHA256withRSAandMGF1", "BC");
        sig2.setParameter(pssParameterSpec);
        sig2.initSign(keyPair.getPrivate());
        sig2.update("另一条消息".getBytes());
        byte[] anotherSignatureBytes = sig2.sign();
        assertNotEquals(
                java.util.Base64.getEncoder().encodeToString(signatureBytes),
                java.util.Base64.getEncoder().encodeToString(anotherSignatureBytes),
                "不同消息的签名应该不同"
        );
    }
}
