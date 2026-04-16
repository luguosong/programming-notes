package com.luguosong.crypto;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.cert.X509CRLHolder;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v2CRLBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CRLConverter;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Security;
import java.security.cert.*;
import java.util.Date;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CRL（证书吊销列表）演示
 * <p>
 * 包含 CRL 的创建和吊销证书的验证
 */
class CrlTest {

    @BeforeAll
    static void setup() {
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
     * 创建一个 V3 CA 证书（供 CRL 使用）
     */
    private static X509Certificate createCACertificate(KeyPair caKeyPair) throws Exception {
        X500Name caName = new X500NameBuilder(BCStyle.INSTANCE)
                .addRDN(BCStyle.CN, "Test CRL CA")
                .addRDN(BCStyle.O, "Test Org")
                .build();

        Date notBefore = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000L);
        Date notAfter = new Date(System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000);

        JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();
        var certBuilder = new JcaX509v3CertificateBuilder(
                caName, BigInteger.valueOf(1), notBefore, notAfter,
                caName, caKeyPair.getPublic()
        );
        certBuilder.addExtension(Extension.subjectKeyIdentifier, false,
                extUtils.createSubjectKeyIdentifier(caKeyPair.getPublic()));
        certBuilder.addExtension(Extension.authorityKeyIdentifier, false,
                extUtils.createAuthorityKeyIdentifier(caKeyPair.getPublic()));

        X509CertificateHolder holder = certBuilder.build(
                new JcaContentSignerBuilder("SHA256WithRSA").setProvider("BC")
                        .build(caKeyPair.getPrivate())
        );
        return new JcaX509CertificateConverter().setProvider("BC").getCertificate(holder);
    }

    /**
     * 创建一个终端实体证书
     */
    private static X509Certificate createEndEntityCert(KeyPair caKeyPair, BigInteger serial) throws Exception {
        KeyPair eeKeyPair = generateRsaKeyPair();
        X500Name caName = new X500NameBuilder(BCStyle.INSTANCE)
                .addRDN(BCStyle.CN, "Test CRL CA")
                .addRDN(BCStyle.O, "Test Org")
                .build();
        X500Name eeName = new X500NameBuilder(BCStyle.INSTANCE)
                .addRDN(BCStyle.CN, "Revoked Server")
                .addRDN(BCStyle.O, "Test Org")
                .build();

        Date notBefore = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000L);
        Date notAfter = new Date(System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000);

        var certBuilder = new JcaX509v3CertificateBuilder(
                caName, serial, notBefore, notAfter,
                eeName, eeKeyPair.getPublic()
        );

        JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();
        certBuilder.addExtension(Extension.subjectKeyIdentifier, false,
                extUtils.createSubjectKeyIdentifier(eeKeyPair.getPublic()));
        certBuilder.addExtension(Extension.authorityKeyIdentifier, false,
                extUtils.createAuthorityKeyIdentifier(caKeyPair.getPublic()));

        X509CertificateHolder holder = certBuilder.build(
                new JcaContentSignerBuilder("SHA256WithRSA").setProvider("BC")
                        .build(caKeyPair.getPrivate())
        );
        return new JcaX509CertificateConverter().setProvider("BC").getCertificate(holder);
    }

    @Test
    void shouldCreateCRL() throws Exception {
        // 1. 生成 CA 密钥对并创建 CA 证书
        KeyPair caKeyPair = generateRsaKeyPair();
        X509Certificate caCert = createCACertificate(caKeyPair);
        System.out.println("CA 证书创建完成: " + caCert.getSubjectX500Principal().getName());

        // 2. 创建两个终端实体证书（用于吊销）
        BigInteger revokedSerial1 = BigInteger.valueOf(1001);
        BigInteger revokedSerial2 = BigInteger.valueOf(1002);
        X509Certificate cert1 = createEndEntityCert(caKeyPair, revokedSerial1);
        X509Certificate cert2 = createEndEntityCert(caKeyPair, revokedSerial2);
        System.out.println("终端实体证书 1 序列号: " + cert1.getSerialNumber());
        System.out.println("终端实体证书 2 序列号: " + cert2.getSerialNumber());

        // 3. 构建 CRL
        X500Name issuerName = new X500Name(caCert.getSubjectX500Principal().getName());
        Date now = new Date();
        X509v2CRLBuilder crlBuilder = new X509v2CRLBuilder(issuerName, now);

        // 4. 添加吊销条目（指定吊销时间和吊销原因）
        //    RevocationReason.superseded 表示证书已被新证书取代
        crlBuilder.addCRLEntry(revokedSerial1, now, org.bouncycastle.asn1.x509.CRLReason.superseded);
        crlBuilder.addCRLEntry(revokedSerial2, now, org.bouncycastle.asn1.x509.CRLReason.keyCompromise);

        // 5. 添加 AuthorityKeyIdentifier 扩展（帮助验证 CRL 的签发者）
        JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();
        crlBuilder.addExtension(
                Extension.authorityKeyIdentifier,
                false,
                extUtils.createAuthorityKeyIdentifier(caKeyPair.getPublic())
        );

        // 6. 签发 CRL
        X509CRLHolder crlHolder = crlBuilder.build(
                new JcaContentSignerBuilder("SHA256WithRSA").setProvider("BC")
                        .build(caKeyPair.getPrivate())
        );

        // 7. 转换为 java.security.cert.X509CRL
        X509CRL crl = new JcaX509CRLConverter().setProvider("BC").getCRL(crlHolder);

        // 8. 验证 CRL 签名
        crl.verify(caCert.getPublicKey());
        System.out.println("CRL 签名验证通过");

        // 9. 断言检查
        assertEquals(2, crl.getRevokedCertificates().size(), "应包含 2 个吊销条目");
        assertNotNull(crl.getRevokedCertificate(revokedSerial1), "序列号 1001 应在 CRL 中");
        assertNotNull(crl.getRevokedCertificate(revokedSerial2), "序列号 1002 应在 CRL 中");

        System.out.println("=== CRL 创建成功 ===");
        System.out.println("签发者: " + crl.getIssuerX500Principal().getName());
        System.out.println("更新时间: " + crl.getThisUpdate());
        System.out.println("吊销条目数: " + crl.getRevokedCertificates().size());

        // 打印每个吊销条目的详细信息
        Set<? extends X509CRLEntry> revokedEntries = crl.getRevokedCertificates();
        for (X509CRLEntry entry : revokedEntries) {
            System.out.println("  序列号: " + entry.getSerialNumber()
                    + ", 吊销时间: " + entry.getRevocationDate()
                    + ", 原因: " + entry.getRevocationReason());
        }
    }

    @Test
    void shouldVerifyRevokedCertificate() throws Exception {
        // 1. 准备 CA 和终端实体证书
        KeyPair caKeyPair = generateRsaKeyPair();
        X509Certificate caCert = createCACertificate(caKeyPair);

        // 创建一个正常证书和一个被吊销的证书
        BigInteger validSerial = BigInteger.valueOf(2001);
        BigInteger revokedSerial = BigInteger.valueOf(2002);
        X509Certificate validCert = createEndEntityCert(caKeyPair, validSerial);
        X509Certificate revokedCert = createEndEntityCert(caKeyPair, revokedSerial);
        System.out.println("正常证书序列号: " + validCert.getSerialNumber());
        System.out.println("被吊销证书序列号: " + revokedCert.getSerialNumber());

        // 2. 创建 CRL，只吊销 revokedSerial 对应的证书
        X500Name issuerName = new X500Name(caCert.getSubjectX500Principal().getName());
        Date now = new Date();
        X509v2CRLBuilder crlBuilder = new X509v2CRLBuilder(issuerName, now);

        crlBuilder.addCRLEntry(revokedSerial, now, org.bouncycastle.asn1.x509.CRLReason.affiliationChanged);

        JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();
        crlBuilder.addExtension(
                Extension.authorityKeyIdentifier, false,
                extUtils.createAuthorityKeyIdentifier(caKeyPair.getPublic())
        );

        X509CRLHolder crlHolder = crlBuilder.build(
                new JcaContentSignerBuilder("SHA256WithRSA").setProvider("BC")
                        .build(caKeyPair.getPrivate())
        );
        X509CRL crl = new JcaX509CRLConverter().setProvider("BC").getCRL(crlHolder);

        // 3. 验证 CRL 签名
        crl.verify(caCert.getPublicKey());

        // 4. 检查被吊销的证书：序列号应在 CRL 中
        X509CRLEntry revokedEntry = crl.getRevokedCertificate(revokedSerial);
        assertNotNull(revokedEntry, "被吊销证书的序列号应在 CRL 中");
        assertEquals(revokedSerial, revokedEntry.getSerialNumber());
        System.out.println("被吊销证书验证: 在 CRL 中找到, 原因: " + revokedEntry.getRevocationReason());

        // 5. 检查正常证书：序列号不应在 CRL 中
        X509CRLEntry validEntry = crl.getRevokedCertificate(validSerial);
        assertNull(validEntry, "正常证书的序列号不应在 CRL 中");
        System.out.println("正常证书验证: 不在 CRL 中（状态正常）");

        // 6. 使用 CertPathValidator 进行包含 CRL 检查的路径验证
        //    构建证书链：CA 证书 → 正常证书（应该通过）
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509", "BC");
        CertPath certPath = certFactory.generateCertPath(java.util.List.of(validCert));

        //    配置 PKIX 参数：信任锚为 CA 证书
        TrustAnchor trustAnchor = new TrustAnchor(caCert, null);
        PKIXParameters params = new PKIXParameters(java.util.Set.of(trustAnchor));
        params.setRevocationEnabled(false); // 此处禁用在线 CRL 检查，因为我们手动验证

        //    验证证书路径
        CertPathValidator validator = CertPathValidator.getInstance("PKIX", "BC");
        PKIXCertPathValidatorResult result = (PKIXCertPathValidatorResult) validator.validate(certPath, params);
        assertNotNull(result, "有效证书路径验证应通过");
        assertNotNull(result.getTrustAnchor(), "应返回信任锚");
        System.out.println("证书路径验证通过，信任锚: " + result.getTrustAnchor().getTrustedCert().getSubjectX500Principal().getName());

        // 7. 构建证书链：CA 证书 → 被吊销证书
        CertPath revokedPath = certFactory.generateCertPath(java.util.List.of(revokedCert));

        //    吊销证书的路径验证（通过 PKIX 参数不启用 CRL 检查时仍然能通过签名验证）
        PKIXCertPathValidatorResult revokedResult = (PKIXCertPathValidatorResult) validator.validate(revokedPath, params);
        System.out.println("注意: 未启用 CRL 检查时，吊销证书的路径签名验证仍然通过");
        System.out.println("生产环境中应启用 revocationEnabled=true 并配置 CRL 分发点");
    }
}
