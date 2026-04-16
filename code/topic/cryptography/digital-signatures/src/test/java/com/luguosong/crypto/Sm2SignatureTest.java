package com.luguosong.crypto;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.params.*;
import org.bouncycastle.crypto.signers.SM2Signer;
import org.bouncycastle.jcajce.spec.SM2ParameterSpec;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.security.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SM2 国密数字签名测试
 * <p>
 * SM2 是中国国家密码管理局制定的椭圆曲线公钥密码算法标准，
 * 基于椭圆曲线离散对数问题（ECDLP），使用 SM3 哈希算法和 sm2p256v1 曲线。
 */
class Sm2SignatureTest {

    /** SM2 默认用户 ID（GB/T 32918 标准） */
    private static final byte[] DEFAULT_ID = "1234567812345678".getBytes();

    @BeforeAll
    static void setUp() {
        // 注册 BouncyCastle Provider
        Security.addProvider(new BouncyCastleProvider());
    }

    @Test
    @DisplayName("SM2 签名与验证 - 使用 BC 高级 API 和低级 API")
    void shouldSignAndVerifyWithSM2() throws Exception {
        // ========== 方式一：使用 BouncyCastle 高级 API（JCA 兼容） ==========

        // 1. 生成 SM2 密钥对（使用 sm2p256v1 曲线）
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC", "BC");
        ECNamedCurveParameterSpec sm2CurveSpec = ECNamedCurveTable.getParameterSpec("sm2p256v1");
        keyPairGenerator.initialize(sm2CurveSpec, new SecureRandom());
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        String originalMessage = "SM2 国密签名测试消息";
        byte[] messageBytes = originalMessage.getBytes();

        // 2. 用私钥签名（使用 SM3withSM2 算法）
        Signature signature = Signature.getInstance("SM3withSM2", "BC");
        // 设置 SM2 签名参数（默认用户 ID 为 "1234567812345678"）
        signature.setParameter(new SM2ParameterSpec(DEFAULT_ID));
        signature.initSign(keyPair.getPrivate());
        signature.update(messageBytes);
        byte[] signatureBytes = signature.sign();

        System.out.println("SM2 签名长度: " + signatureBytes.length + " 字节");
        // SM2 签名由 (r, s) 两个 32 字节的整数组成，DER 编码后通常为 70-72 字节
        assertTrue(signatureBytes.length >= 70 && signatureBytes.length <= 72,
                "SM2 签名长度应在 70-72 字节范围内");

        // 3. 用公钥验证签名
        signature.setParameter(new SM2ParameterSpec(DEFAULT_ID));
        signature.initVerify(keyPair.getPublic());
        signature.update(messageBytes);
        assertTrue(signature.verify(signatureBytes), "签名验证应该通过");

        // 4. 验证：篡改消息后签名应该无法通过
        signature.setParameter(new SM2ParameterSpec(DEFAULT_ID));
        signature.initVerify(keyPair.getPublic());
        signature.update("被篡改的消息".getBytes());
        assertFalse(signature.verify(signatureBytes), "篡改消息后签名验证应该失败");

        // 5. 验证：不同消息产生的签名不同
        Signature sig2 = Signature.getInstance("SM3withSM2", "BC");
        sig2.setParameter(new SM2ParameterSpec(DEFAULT_ID));
        sig2.initSign(keyPair.getPrivate());
        sig2.update("另一条消息".getBytes());
        byte[] anotherSignatureBytes = sig2.sign();
        assertNotEquals(
                java.util.Base64.getEncoder().encodeToString(signatureBytes),
                java.util.Base64.getEncoder().encodeToString(anotherSignatureBytes),
                "不同消息的签名应该不同"
        );

        // ========== 方式二：使用 BouncyCastle 低级 API ==========

        // 6. 从 JCE 曲线参数获取低级 API 需要的 ECDomainParameters
        ECDomainParameters domainParams = new ECDomainParameters(
                sm2CurveSpec.getCurve(),
                sm2CurveSpec.getG(),
                sm2CurveSpec.getN(),
                sm2CurveSpec.getH()
        );

        // 7. 通过低级 API 生成密钥对
        ECKeyPairGenerator lowLevelKpg = new ECKeyPairGenerator();
        lowLevelKpg.init(new ECKeyGenerationParameters(domainParams, new SecureRandom()));
        AsymmetricCipherKeyPair lowLevelKeyPair = lowLevelKpg.generateKeyPair();

        ECPrivateKeyParameters privateKeyParams =
                (ECPrivateKeyParameters) lowLevelKeyPair.getPrivate();
        ECPublicKeyParameters publicKeyParams =
                (ECPublicKeyParameters) lowLevelKeyPair.getPublic();

        // 8. 使用低级 SM2Signer 签名
        //    参数嵌套顺序：ParametersWithID(ParametersWithRandom(keyParams, random), id)
        //    SM2Signer.init 先解包外层 ParametersWithID 获取用户 ID，
        //    再对解包后的 ParametersWithRandom 解包获取密钥参数
        SM2Signer sm2Signer = new SM2Signer(new SM3Digest());
        sm2Signer.init(true,
                new ParametersWithID(
                        new ParametersWithRandom(privateKeyParams, new SecureRandom()),
                        DEFAULT_ID
                )
        );
        sm2Signer.update(messageBytes, 0, messageBytes.length);
        byte[] lowLevelSignature = sm2Signer.generateSignature();

        System.out.println("SM2 低级 API 签名长度: " + lowLevelSignature.length + " 字节");

        // 9. 使用低级 API 验证签名（验签只需 ParametersWithID 包装公钥）
        SM2Signer verifier = new SM2Signer(new SM3Digest());
        verifier.init(false, new ParametersWithID(publicKeyParams, DEFAULT_ID));
        verifier.update(messageBytes, 0, messageBytes.length);
        assertTrue(verifier.verifySignature(lowLevelSignature), "低级 API 签名验证应该通过");

        // 10. 验证：篡改消息后低级 API 签名验证失败
        byte[] tamperedMessage = "被篡改的消息".getBytes();
        SM2Signer tamperedVerifier = new SM2Signer(new SM3Digest());
        tamperedVerifier.init(false, new ParametersWithID(publicKeyParams, DEFAULT_ID));
        tamperedVerifier.update(tamperedMessage, 0, tamperedMessage.length);
        assertFalse(tamperedVerifier.verifySignature(lowLevelSignature),
                "低级 API 篡改消息后签名验证应该失败");
    }
}
