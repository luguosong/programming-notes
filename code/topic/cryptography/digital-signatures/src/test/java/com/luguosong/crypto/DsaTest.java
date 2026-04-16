package com.luguosong.crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DSA 系列数字签名测试：DSA、ECDSA、EdDSA（Ed25519）
 */
class DsaTest {

    @BeforeAll
    static void setUp() {
        // 注册 BouncyCastle Provider，使 JCA 能够识别 BC 提供的算法
        Security.addProvider(new BouncyCastleProvider());
    }

    @Test
    @DisplayName("DSA 签名与验证 - SHA256withDSA")
    void shouldSignAndVerifyWithDSA() throws Exception {
        // 1. 生成 DSA 密钥对（密钥长度 2048 位）
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DSA", "BC");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        // 2. 准备待签名的原始数据
        String originalMessage = "DSA 签名测试消息";
        byte[] messageBytes = originalMessage.getBytes();

        // 3. 用私钥签名
        Signature signature = Signature.getInstance("SHA256withDSA", "BC");
        signature.initSign(keyPair.getPrivate());
        signature.update(messageBytes);
        byte[] signatureBytes = signature.sign();

        System.out.println("DSA 签名长度: " + signatureBytes.length + " 字节");

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
        Signature anotherSig = Signature.getInstance("SHA256withDSA", "BC");
        anotherSig.initSign(keyPair.getPrivate());
        anotherSig.update("另一条消息".getBytes());
        byte[] anotherSignatureBytes = anotherSig.sign();
        assertNotEquals(
                java.util.Base64.getEncoder().encodeToString(signatureBytes),
                java.util.Base64.getEncoder().encodeToString(anotherSignatureBytes),
                "不同消息的签名应该不同"
        );
    }

    @Test
    @DisplayName("ECDSA 签名与验证 - SHA256withECDSA（secp256r1）")
    void shouldSignAndVerifyWithECDSA() throws Exception {
        // 1. 生成 ECDSA 密钥对，使用 secp256r1（即 NIST P-256）曲线
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("ECDSA", "BC");
        keyPairGenerator.initialize(new java.security.spec.ECGenParameterSpec("secp256r1"));
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        // 2. 准备待签名的原始数据
        String originalMessage = "ECDSA 签名测试消息";
        byte[] messageBytes = originalMessage.getBytes();

        // 3. 用私钥签名
        Signature signature = Signature.getInstance("SHA256withECDSA", "BC");
        signature.initSign(keyPair.getPrivate());
        signature.update(messageBytes);
        byte[] signatureBytes = signature.sign();

        System.out.println("ECDSA 签名长度: " + signatureBytes.length + " 字节");
        System.out.println("ECDSA 公钥算法: " + keyPair.getPublic().getAlgorithm());
        System.out.println("ECDSA 公钥格式: " + keyPair.getPublic().getFormat());

        // 4. 用公钥验证签名
        signature.initVerify(keyPair.getPublic());
        signature.update(messageBytes);
        assertTrue(signature.verify(signatureBytes), "签名验证应该通过");

        // 5. 验证：篡改消息后签名应该无法通过
        byte[] tamperedBytes = "被篡改的消息".getBytes();
        signature.initVerify(keyPair.getPublic());
        signature.update(tamperedBytes);
        assertFalse(signature.verify(signatureBytes), "篡改消息后签名验证应该失败");

        // 6. 验证：不同消息产生的签名不同（ECDSA 每次签名含随机数 k，同一消息签名也不同）
        Signature sig2 = Signature.getInstance("SHA256withECDSA", "BC");
        sig2.initSign(keyPair.getPrivate());
        sig2.update(messageBytes);
        byte[] signatureBytes2 = sig2.sign();
        assertNotEquals(
                java.util.Base64.getEncoder().encodeToString(signatureBytes),
                java.util.Base64.getEncoder().encodeToString(signatureBytes2),
                "同一消息两次签名应该不同（ECDSA 的随机性）"
        );
    }

    @Test
    @DisplayName("EdDSA 签名与验证 - Ed25519")
    void shouldSignAndVerifyWithEd25519() throws Exception {
        // 1. 生成 Ed25519 密钥对
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("Ed25519", "BC");
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        // 2. 准备待签名的原始数据
        String originalMessage = "Ed25519 签名测试消息";
        byte[] messageBytes = originalMessage.getBytes();

        // 3. 用私钥签名（EdDSA 内部自带 SHA-512 哈希，直接传入原始消息即可）
        Signature signature = Signature.getInstance("EdDSA", "BC");
        signature.initSign(keyPair.getPrivate());
        signature.update(messageBytes);
        byte[] signatureBytes = signature.sign();

        System.out.println("Ed25519 签名长度: " + signatureBytes.length + " 字节");
        System.out.println("Ed25519 公钥长度: " + keyPair.getPublic().getEncoded().length + " 字节");

        // 4. 用公钥验证签名
        signature.initVerify(keyPair.getPublic());
        signature.update(messageBytes);
        assertTrue(signature.verify(signatureBytes), "签名验证应该通过");

        // 5. 验证：篡改消息后签名应该无法通过
        signature.initVerify(keyPair.getPublic());
        signature.update("被篡改的消息".getBytes());
        assertFalse(signature.verify(signatureBytes), "篡改消息后签名验证应该失败");

        // 6. 验证：不同消息产生的签名不同（Ed25519 是确定性签名，同一消息+同一私钥签名相同，
        //    但不同消息签名必然不同）
        Signature sig2 = Signature.getInstance("EdDSA", "BC");
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
