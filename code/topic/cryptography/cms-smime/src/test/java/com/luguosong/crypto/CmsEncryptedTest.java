package com.luguosong.crypto;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.cms.CMSAlgorithm;
import org.bouncycastle.cms.CMSEnvelopedData;
import org.bouncycastle.cms.CMSEnvelopedDataGenerator;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.RecipientInformation;
import org.bouncycastle.cms.RecipientInformationStore;
import org.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import org.bouncycastle.cms.jcajce.JceKeyTransEnvelopedRecipient;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator;
import org.bouncycastle.cms.jcajce.JcePasswordEnvelopedRecipient;
import org.bouncycastle.cms.jcajce.JcePasswordRecipientInfoGenerator;
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
 * CMS 加密演示
 * <p>
 * CMS 支持两种加密方式：
 * - 密钥传输（Key Transport）：用 RSA 等非对称算法包装对称密钥
 * - 密码加密（Password-based）：用密码派生密钥加密对称密钥
 */
class CmsEncryptedTest {

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
    void shouldEncryptAndDecryptWithCMS() throws Exception {
        // 1. 生成收件人的 RSA 密钥对和证书
        KeyPair recipientKeyPair = generateRsaKeyPair();
        X509Certificate recipientCert = generateSelfSignedCert(recipientKeyPair, "CMS Recipient");
        System.out.println("收件人证书: " + recipientCert.getSubjectX500Principal().getName());

        // 2. 原始数据
        byte[] originalData = "这是一条需要 CMS 加密保护的机密消息".getBytes();
        System.out.println("原始数据: " + new String(originalData));

        // 3. 创建 CMS 加密生成器
        CMSEnvelopedDataGenerator envelopedGenerator = new CMSEnvelopedDataGenerator();

        // 4. 添加收件人信息（使用 RSA 密钥传输）
        //    收件人的公钥证书用于加密对称密钥
        envelopedGenerator.addRecipientInfoGenerator(
                new JceKeyTransRecipientInfoGenerator(recipientCert)
                        .setProvider("BC")
        );

        // 5. 使用 AES-256-CBC 加密内容
        CMSEnvelopedData envelopedData = envelopedGenerator.generate(
                new CMSProcessableByteArray(originalData),
                new JceCMSContentEncryptorBuilder(CMSAlgorithm.AES256_CBC)
                        .setProvider("BC")
                        .build()
        );

        byte[] encryptedBytes = envelopedData.getEncoded();
        System.out.println("CMS 加密数据大小: " + encryptedBytes.length + " bytes");
        System.out.println("加密算法: " + envelopedData.getEncryptionAlgOID());

        // 6. 解密：用收件人私钥解密
        CMSEnvelopedData envelopedDataToDecrypt = new CMSEnvelopedData(encryptedBytes);

        // 7. 获取收件人信息并解密
        RecipientInformationStore recipientInfoStore = envelopedDataToDecrypt.getRecipientInfos();
        Collection<RecipientInformation> recipients = recipientInfoStore.getRecipients();
        assertFalse(recipients.isEmpty(), "应至少有一个收件人");

        RecipientInformation recipientInfo = recipients.iterator().next();

        // 8. 使用收件人私钥创建解密器
        byte[] decryptedData = recipientInfo.getContent(
                new JceKeyTransEnvelopedRecipient(recipientKeyPair.getPrivate())
                        .setProvider("BC")
        );

        // 9. 验证解密结果
        assertArrayEquals(originalData, decryptedData, "解密后的数据应与原始数据一致");

        System.out.println("=== CMS 密钥传输加密解密成功 ===");
        System.out.println("解密数据: " + new String(decryptedData));
    }

    @Test
    void shouldEncryptAndDecryptWithPassword() throws Exception {
        // 1. 原始数据
        byte[] originalData = "这是一条通过密码保护的 CMS 加密消息".getBytes();
        System.out.println("原始数据: " + new String(originalData));

        // 2. 加密密码
        char[] password = "my-secret-password".toCharArray();
        System.out.println("密码长度: " + password.length);

        // 3. 创建 CMS 加密生成器
        CMSEnvelopedDataGenerator envelopedGenerator = new CMSEnvelopedDataGenerator();

        // 4. 添加密码收件人信息（使用 PBKDF2 密码派生）
        //    第一个参数指定 KEK（密钥加密密钥）算法 OID，
        //    BC 用此 OID 查找密钥长度（AES256_CBC → 256 位）
        JcePasswordRecipientInfoGenerator passwordRecipientInfoGenerator =
                new JcePasswordRecipientInfoGenerator(
                        CMSAlgorithm.AES256_CBC,  // KEK 算法 OID，决定派生密钥长度
                        password
                ).setProvider("BC");
        envelopedGenerator.addRecipientInfoGenerator(passwordRecipientInfoGenerator);

        // 5. 使用 AES-256-CBC 加密内容
        CMSEnvelopedData envelopedData = envelopedGenerator.generate(
                new CMSProcessableByteArray(originalData),
                new JceCMSContentEncryptorBuilder(CMSAlgorithm.AES256_CBC)
                        .setProvider("BC")
                        .build()
        );

        byte[] encryptedBytes = envelopedData.getEncoded();
        System.out.println("CMS 密码加密数据大小: " + encryptedBytes.length + " bytes");
        System.out.println("加密算法: " + envelopedData.getEncryptionAlgOID());

        // 6. 解密：用相同密码解密
        CMSEnvelopedData envelopedDataToDecrypt = new CMSEnvelopedData(encryptedBytes);

        RecipientInformationStore recipientInfoStore = envelopedDataToDecrypt.getRecipientInfos();
        Collection<RecipientInformation> recipients = recipientInfoStore.getRecipients();
        assertFalse(recipients.isEmpty(), "应至少有一个收件人");

        RecipientInformation recipientInfo = recipients.iterator().next();

        // 7. 使用密码创建解密器
        byte[] decryptedData = recipientInfo.getContent(
                new JcePasswordEnvelopedRecipient(password)
                        .setProvider("BC")
        );

        // 8. 验证解密结果
        assertArrayEquals(originalData, decryptedData, "解密后的数据应与原始数据一致");

        System.out.println("=== CMS 密码加密解密成功 ===");
        System.out.println("解密数据: " + new String(decryptedData));
    }
}
