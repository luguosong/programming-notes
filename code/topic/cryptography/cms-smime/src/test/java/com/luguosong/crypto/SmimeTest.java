package com.luguosong.crypto;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.cms.CMSAlgorithm;
import org.bouncycastle.cms.RecipientInformation;
import org.bouncycastle.cms.RecipientInformationStore;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cms.SignerInformationVerifier;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoGeneratorBuilder;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import org.bouncycastle.cms.jcajce.JceKeyTransEnvelopedRecipient;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.mail.smime.SMIMEEnveloped;
import org.bouncycastle.mail.smime.SMIMEEnvelopedGenerator;
import org.bouncycastle.mail.smime.SMIMESigned;
import org.bouncycastle.mail.smime.SMIMESignedGenerator;
import org.bouncycastle.mail.smime.SMIMEUtil;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * S/MIME（Secure/Multipurpose Internet Mail Extensions）演示
 * <p>
 * S/MIME 基于 CMS 语法，为电子邮件提供签名和加密功能：
 * - 签名邮件：multipart/signed 格式，确保邮件完整性和发送方身份
 * - 加密邮件：application/pkcs7-mime 格式，确保邮件机密性
 */
class SmimeTest {

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

    /**
     * 创建一封简单的文本邮件
     */
    private static MimeMessage createSimpleMessage(String from, String to, String subject, String text) throws Exception {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props);

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        message.setSubject(subject);
        message.setText(text);
        message.saveChanges();

        return message;
    }

    @Test
    void shouldCreateAndVerifySmimeSignedMessage() throws Exception {
        // 1. 准备签名者的密钥对和证书
        KeyPair signerKeyPair = generateRsaKeyPair();
        X509Certificate signerCert = generateSelfSignedCert(signerKeyPair, "S/MIME Signer");
        System.out.println("签名者证书: " + signerCert.getSubjectX500Principal().getName());

        // 2. 创建邮件正文部分
        MimeBodyPart bodyPart = new MimeBodyPart();
        bodyPart.setText("这是一封通过 S/MIME 签名的测试邮件。");
        System.out.println("邮件正文: " + bodyPart.getContent());

        // 3. 创建 S/MIME 签名生成器
        SMIMESignedGenerator smimeSignedGenerator = new SMIMESignedGenerator();

        // 4. 添加签名者信息（使用 SHA256WithRSA 签名算法）
        smimeSignedGenerator.addSignerInfoGenerator(
                new JcaSimpleSignerInfoGeneratorBuilder()
                        .setProvider("BC")
                        .build("SHA256WithRSA", signerKeyPair.getPrivate(), signerCert)
        );

        // 5. 添加证书链（收件人验证签名时需要）
        smimeSignedGenerator.addCertificates(
                new JcaCertStore(List.of(signerCert))
        );

        // 6. 生成签名邮件（multipart/signed 格式）
        MimeMultipart signedMultipart = smimeSignedGenerator.generate(bodyPart);
        System.out.println("签名邮件内容类型: " + signedMultipart.getContentType());

        // 7. 构建完整的签名邮件消息
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props);
        MimeMessage signedMessage = new MimeMessage(session);
        signedMessage.setFrom(new InternetAddress("sender@example.com"));
        signedMessage.setRecipient(Message.RecipientType.TO, new InternetAddress("receiver@example.com"));
        signedMessage.setSubject("S/MIME 签名测试");
        signedMessage.setContent(signedMultipart);
        signedMessage.saveChanges();

        // 8. 验证签名：从 multipart/signed 中解析签名
        SMIMESigned signed = new SMIMESigned(
                (MimeMultipart) signedMessage.getContent()
        );

        // 9. 获取签名信息并验证
        SignerInformationStore signerInfoStore = signed.getSignerInfos();
        Collection<SignerInformation> signers = signerInfoStore.getSigners();
        assertFalse(signers.isEmpty(), "应至少有一个签名");

        SignerInformation signerInfo = signers.iterator().next();

        // 10. 使用签名者证书验证签名
        SignerInformationVerifier verifier =
                new JcaSimpleSignerInfoVerifierBuilder()
                        .setProvider("BC")
                        .build(signerCert.getPublicKey());

        assertTrue(signerInfo.verify(verifier), "S/MIME 签名验证应通过");

        // 11. 提取签名邮件中的原始内容
        MimeBodyPart signedContent = signed.getContent();
        String extractedText = (String) signedContent.getContent();
        assertEquals("这是一封通过 S/MIME 签名的测试邮件。", extractedText,
                "提取的邮件内容应一致");
        System.out.println("签名邮件中提取的正文: " + extractedText);

        System.out.println("=== S/MIME 签名邮件创建与验证成功 ===");
        System.out.println("签名算法: " + signerInfo.getDigestAlgorithmID().getAlgorithm().getId());
        System.out.println("签名者: " + signerCert.getSubjectX500Principal().getName());
    }

    @Test
    void shouldEncryptAndDecryptSmimeMessage() throws Exception {
        // 1. 准备收件人的密钥对和证书
        KeyPair recipientKeyPair = generateRsaKeyPair();
        X509Certificate recipientCert = generateSelfSignedCert(recipientKeyPair, "S/MIME Recipient");
        System.out.println("收件人证书: " + recipientCert.getSubjectX500Principal().getName());

        // 2. 创建原始邮件正文
        MimeBodyPart bodyPart = new MimeBodyPart();
        bodyPart.setText("这是一封通过 S/MIME 加密的机密邮件。");
        System.out.println("原始邮件正文: " + bodyPart.getContent());

        // 3. 创建 S/MIME 加密生成器
        SMIMEEnvelopedGenerator smimeEnvelopedGenerator = new SMIMEEnvelopedGenerator();

        // 4. 添加收件人信息（使用 RSA 密钥传输）
        smimeEnvelopedGenerator.addRecipientInfoGenerator(
                new JceKeyTransRecipientInfoGenerator(recipientCert)
                        .setProvider("BC")
        );

        // 5. 加密邮件正文（使用 AES-256-CBC）
        MimeBodyPart encryptedPart = smimeEnvelopedGenerator.generate(
                bodyPart,
                new JceCMSContentEncryptorBuilder(CMSAlgorithm.AES256_CBC)
                        .setProvider("BC")
                        .build()
        );

        System.out.println("加密邮件内容类型: " + encryptedPart.getContentType());

        // 6. 解密邮件：直接从加密的 MimeBodyPart 解析
        SMIMEEnveloped enveloped = new SMIMEEnveloped(encryptedPart);

        // 7. 获取收件人信息并解密
        RecipientInformationStore recipientInfoStore = enveloped.getRecipientInfos();
        Collection<RecipientInformation> recipients = recipientInfoStore.getRecipients();
        assertFalse(recipients.isEmpty(), "应至少有一个收件人");

        RecipientInformation recipientInfo = recipients.iterator().next();

        // 8. 使用收件人私钥解密，得到 MimeBodyPart
        MimeBodyPart decryptedPart = SMIMEUtil.toMimeBodyPart(
                recipientInfo.getContent(
                        new JceKeyTransEnvelopedRecipient(recipientKeyPair.getPrivate())
                                .setProvider("BC")
                )
        );

        // 9. 验证解密结果
        String decryptedText = (String) decryptedPart.getContent();
        System.out.println("解密邮件正文: " + decryptedText);

        assertEquals("这是一封通过 S/MIME 加密的机密邮件。", decryptedText,
                "解密后的邮件内容应与原始邮件一致");

        System.out.println("=== S/MIME 加密邮件创建与解密成功 ===");
    }
}
