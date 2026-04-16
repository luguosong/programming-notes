package com.luguosong.crypto;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cms.SignerInformationVerifier;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoGeneratorBuilder;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CMS（Cryptographic Message Syntax）签名演示
 * <p>
 * CMS 定义了数字签名的封装格式，有两种模式：
 * - Detached：签名与原始数据分离，接收方需要同时持有数据和签名
 * - Enveloping：签名与原始数据打包在一起，自包含
 */
class CmsSignedTest {

    @BeforeAll
    static void setup() {
        // 注册 BouncyCastle Provider
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * 生成 RSA 2048 密钥对
     */
    private static KeyPair generateRsaKeyPair() throws Exception {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA", "BC");
        keyPairGen.initialize(2048);
        return keyPairGen.generateKeyPair();
    }

    /**
     * 生成自签名 X.509 证书
     */
    private static X509Certificate generateSelfSignedCert(KeyPair keyPair, String cn) throws Exception {
        X500NameBuilder nameBuilder = new X500NameBuilder(BCStyle.INSTANCE);
        nameBuilder.addRDN(BCStyle.CN, cn);
        nameBuilder.addRDN(BCStyle.O, "Test Org");
        X500Name subjectName = nameBuilder.build();

        Date notBefore = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000L);
        Date notAfter = new Date(System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000);

        return new JcaX509CertificateConverter()
                .setProvider("BC")
                .getCertificate(new JcaX509v3CertificateBuilder(
                        subjectName,
                        BigInteger.valueOf(System.currentTimeMillis()),
                        notBefore,
                        notAfter,
                        subjectName,
                        keyPair.getPublic()
                ).build(new JcaContentSignerBuilder("SHA256WithRSA")
                        .setProvider("BC")
                        .build(keyPair.getPrivate())));
    }

    @Test
    void shouldCreateAndVerifyDetachedSignature() throws Exception {
        // 1. 准备密钥对和自签名证书
        KeyPair keyPair = generateRsaKeyPair();
        X509Certificate cert = generateSelfSignedCert(keyPair, "CMS Detached Signer");
        System.out.println("证书主体: " + cert.getSubjectX500Principal().getName());

        // 2. 原始数据
        byte[] originalData = "这是一条用于 CMS Detached 签名的测试数据".getBytes();
        System.out.println("原始数据: " + new String(originalData));

        // 3. 创建 CMS 签名生成器，使用 SHA256WithRSA 签名算法
        CMSSignedDataGenerator signedDataGenerator = new CMSSignedDataGenerator();
        signedDataGenerator.addSignerInfoGenerator(
                new JcaSimpleSignerInfoGeneratorBuilder()
                        .setProvider("BC")
                        .build("SHA256WithRSA", keyPair.getPrivate(), cert)
        );

        // 4. 生成 Detached 签名（签名与数据分离）
        CMSSignedData signedData = signedDataGenerator.generate(
                new CMSProcessableByteArray(originalData),
                true  // detached = true，签名不包含原始数据
        );

        byte[] signatureBytes = signedData.getEncoded();
        System.out.println("Detached 签名大小: " + signatureBytes.length + " bytes");

        // 5. 验证签名：将原始数据和签名重新组合
        CMSSignedData signedDataToVerify = new CMSSignedData(
                new CMSProcessableByteArray(originalData),
                signatureBytes
        );

        // 6. 构建签名验证器
        SignerInformationVerifier verifier = new JcaSimpleSignerInfoVerifierBuilder()
                .setProvider("BC")
                .build(cert.getPublicKey());

        // 7. 遍历签名信息并逐个验证
        SignerInformationStore signerInfoStore = signedDataToVerify.getSignerInfos();
        Collection<SignerInformation> signers = signerInfoStore.getSigners();
        assertFalse(signers.isEmpty(), "应至少有一个签名");

        SignerInformation signerInfo = signers.iterator().next();
        assertTrue(signerInfo.verify(verifier), "Detached 签名验证应通过");

        System.out.println("=== CMS Detached 签名验证通过 ===");
        System.out.println("签名算法: " + signerInfo.getDigestAlgorithmID().getAlgorithm().getId());
        System.out.println("签名者证书: " + cert.getSubjectX500Principal().getName());
    }

    @Test
    void shouldCreateAndVerifyEnvelopedSignature() throws Exception {
        // 1. 准备密钥对和自签名证书
        KeyPair keyPair = generateRsaKeyPair();
        X509Certificate cert = generateSelfSignedCert(keyPair, "CMS Enveloped Signer");
        System.out.println("证书主体: " + cert.getSubjectX500Principal().getName());

        // 2. 原始数据
        byte[] originalData = "这是一条用于 CMS Enveloping 签名的测试数据".getBytes();
        System.out.println("原始数据: " + new String(originalData));

        // 3. 创建 CMS 签名生成器
        CMSSignedDataGenerator signedDataGenerator = new CMSSignedDataGenerator();
        signedDataGenerator.addSignerInfoGenerator(
                new JcaSimpleSignerInfoGeneratorBuilder()
                        .setProvider("BC")
                        .build("SHA256WithRSA", keyPair.getPrivate(), cert)
        );

        // 4. 生成 Enveloping 签名（签名+数据打包在一起）
        CMSSignedData signedData = signedDataGenerator.generate(
                new CMSProcessableByteArray(originalData),
                false  // detached = false，签名中包含原始数据
        );

        byte[] signedContent = signedData.getEncoded();
        System.out.println("Enveloping 签名（含数据）大小: " + signedContent.length + " bytes");

        // 5. 验证签名：只需要签名数据即可验证（因为数据已嵌入签名中）
        //    注意：直接从 byte[] 构造的 CMSSignedData 不保留嵌入内容，
        //    需要传入 CMSProcessable 以保留内容供后续提取
        CMSSignedData signedDataToVerify = new CMSSignedData(
                signedData.getSignedContent(),
                signedContent
        );

        // 6. 从嵌入数据中提取原始内容
        byte[] extractedData = (byte[]) signedDataToVerify.getSignedContent().getContent();
        assertArrayEquals(originalData, extractedData, "提取的数据应与原始数据一致");
        System.out.println("从 Enveloping 签名中提取的数据: " + new String(extractedData));

        // 7. 验证签名
        SignerInformationVerifier verifier = new JcaSimpleSignerInfoVerifierBuilder()
                .setProvider("BC")
                .build(cert.getPublicKey());

        SignerInformationStore signerInfoStore = signedDataToVerify.getSignerInfos();
        Collection<SignerInformation> signers = signerInfoStore.getSigners();
        assertFalse(signers.isEmpty(), "应至少有一个签名");

        SignerInformation signerInfo = signers.iterator().next();
        assertTrue(signerInfo.verify(verifier), "Enveloping 签名验证应通过");

        System.out.println("=== CMS Enveloping 签名验证通过 ===");
        System.out.println("签名算法: " + signerInfo.getDigestAlgorithmID().getAlgorithm().getId());
        System.out.println("签名者证书: " + cert.getSubjectX500Principal().getName());
    }
}
