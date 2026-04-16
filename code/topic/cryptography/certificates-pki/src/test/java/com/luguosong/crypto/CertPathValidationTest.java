package com.luguosong.crypto;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.cert.X509CertificateHolder;
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
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 证书路径验证演示
 * <p>
 * 使用 PKIX 算法验证证书链：信任锚（CA）→ 终端实体证书
 */
class CertPathValidationTest {

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
     * 创建 V3 CA 证书，包含 BasicConstraints 和 KeyUsage 扩展
     */
    private static X509Certificate createRootCACert(KeyPair caKeyPair) throws Exception {
        X500Name caName = new X500NameBuilder(BCStyle.INSTANCE)
                .addRDN(BCStyle.CN, "Root CA")
                .addRDN(BCStyle.O, "PKI Test Org")
                .addRDN(BCStyle.C, "CN")
                .build();

        Date notBefore = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000L);
        Date notAfter = new Date(System.currentTimeMillis() + 3650L * 24 * 60 * 60 * 1000);  // 10 年

        JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();
        var certBuilder = new JcaX509v3CertificateBuilder(
                caName, BigInteger.valueOf(1), notBefore, notAfter,
                caName, caKeyPair.getPublic()
        );

        // SubjectKeyIdentifier：标识此 CA 的公钥
        certBuilder.addExtension(Extension.subjectKeyIdentifier, false,
                extUtils.createSubjectKeyIdentifier(caKeyPair.getPublic()));
        // AuthorityKeyIdentifier：自签名时指向自身
        certBuilder.addExtension(Extension.authorityKeyIdentifier, false,
                extUtils.createAuthorityKeyIdentifier(caKeyPair.getPublic()));
        // BasicConstraints：CA=true, pathLen=1（允许签发 1 级中间 CA）
        certBuilder.addExtension(Extension.basicConstraints, true,
                new BasicConstraints(1));
        // KeyUsage：CA 证书可签发证书和 CRL
        certBuilder.addExtension(Extension.keyUsage, true,
                new KeyUsage(KeyUsage.keyCertSign | KeyUsage.cRLSign));

        X509CertificateHolder holder = certBuilder.build(
                new JcaContentSignerBuilder("SHA256WithRSA").setProvider("BC")
                        .build(caKeyPair.getPrivate())
        );
        return new JcaX509CertificateConverter().setProvider("BC").getCertificate(holder);
    }

    /**
     * 创建中间 CA 证书（由 Root CA 签发）
     */
    private static X509Certificate createIntermediateCACert(KeyPair rootKeyPair, X509Certificate rootCert,
                                                            KeyPair intermediateKeyPair) throws Exception {
        X500Name rootName = new X500Name(rootCert.getSubjectX500Principal().getName());
        X500Name intermediateName = new X500NameBuilder(BCStyle.INSTANCE)
                .addRDN(BCStyle.CN, "Intermediate CA")
                .addRDN(BCStyle.O, "PKI Test Org")
                .addRDN(BCStyle.C, "CN")
                .build();

        Date notBefore = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000L);
        Date notAfter = new Date(System.currentTimeMillis() + 1825L * 24 * 60 * 60 * 1000);  // 5 年

        JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();
        var certBuilder = new JcaX509v3CertificateBuilder(
                rootName, BigInteger.valueOf(2), notBefore, notAfter,
                intermediateName, intermediateKeyPair.getPublic()
        );

        certBuilder.addExtension(Extension.subjectKeyIdentifier, false,
                extUtils.createSubjectKeyIdentifier(intermediateKeyPair.getPublic()));
        certBuilder.addExtension(Extension.authorityKeyIdentifier, false,
                extUtils.createAuthorityKeyIdentifier(rootKeyPair.getPublic()));
        // BasicConstraints：CA=true, pathLen=0（不能再签发下级 CA）
        certBuilder.addExtension(Extension.basicConstraints, true,
                new BasicConstraints(0));
        certBuilder.addExtension(Extension.keyUsage, true,
                new KeyUsage(KeyUsage.keyCertSign | KeyUsage.cRLSign));

        X509CertificateHolder holder = certBuilder.build(
                new JcaContentSignerBuilder("SHA256WithRSA").setProvider("BC")
                        .build(rootKeyPair.getPrivate())
        );
        return new JcaX509CertificateConverter().setProvider("BC").getCertificate(holder);
    }

    /**
     * 创建终端实体证书（由中间 CA 签发）
     */
    private static X509Certificate createEndEntityCert(KeyPair intermediateKeyPair,
                                                       X509Certificate intermediateCert,
                                                       String commonName) throws Exception {
        KeyPair eeKeyPair = generateRsaKeyPair();
        X500Name issuerName = new X500Name(intermediateCert.getSubjectX500Principal().getName());
        X500Name eeName = new X500NameBuilder(BCStyle.INSTANCE)
                .addRDN(BCStyle.CN, commonName)
                .addRDN(BCStyle.O, "PKI Test Org")
                .addRDN(BCStyle.C, "CN")
                .build();

        Date notBefore = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000L);
        Date notAfter = new Date(System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000);  // 1 年

        JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();
        var certBuilder = new JcaX509v3CertificateBuilder(
                issuerName, BigInteger.valueOf(100), notBefore, notAfter,
                eeName, eeKeyPair.getPublic()
        );

        certBuilder.addExtension(Extension.subjectKeyIdentifier, false,
                extUtils.createSubjectKeyIdentifier(eeKeyPair.getPublic()));
        certBuilder.addExtension(Extension.authorityKeyIdentifier, false,
                extUtils.createAuthorityKeyIdentifier(intermediateKeyPair.getPublic()));
        // 终端实体的 BasicConstraints：CA=false
        certBuilder.addExtension(Extension.basicConstraints, true,
                new BasicConstraints(false));
        // 终端实体的 KeyUsage：数字签名和密钥加密
        certBuilder.addExtension(Extension.keyUsage, true,
                new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyEncipherment));

        X509CertificateHolder holder = certBuilder.build(
                new JcaContentSignerBuilder("SHA256WithRSA").setProvider("BC")
                        .build(intermediateKeyPair.getPrivate())
        );
        return new JcaX509CertificateConverter().setProvider("BC").getCertificate(holder);
    }

    @Test
    void shouldValidateCertificatePath() throws Exception {
        // === 1. 构建 PKI 证书体系 ===

        // 1a. Root CA（根 CA，自签名）
        KeyPair rootKeyPair = generateRsaKeyPair();
        X509Certificate rootCert = createRootCACert(rootKeyPair);
        System.out.println("[1] Root CA 创建完成: " + rootCert.getSubjectX500Principal().getName());

        // 1b. Intermediate CA（中间 CA，由 Root CA 签发）
        KeyPair intermediateKeyPair = generateRsaKeyPair();
        X509Certificate intermediateCert = createIntermediateCACert(rootKeyPair, rootCert, intermediateKeyPair);
        System.out.println("[2] Intermediate CA 创建完成: " + intermediateCert.getSubjectX500Principal().getName());

        // 1c. End Entity（终端实体证书，由 Intermediate CA 签发）
        X509Certificate eeCert = createEndEntityCert(intermediateKeyPair, intermediateCert, "www.example.com");
        System.out.println("[3] 终端实体证书创建完成: " + eeCert.getSubjectX500Principal().getName());

        // === 2. 验证证书签名 ===

        // 中间 CA 证书应由 Root CA 签名
        intermediateCert.verify(rootCert.getPublicKey());
        System.out.println("中间 CA 签名验证通过（由 Root CA 签发）");

        // 终端实体证书应由中间 CA 签名
        eeCert.verify(intermediateCert.getPublicKey());
        System.out.println("终端实体证书签名验证通过（由 Intermediate CA 签发）");

        // === 3. 构建证书路径 ===
        //    证书路径（从终端实体到信任锚）:
        //    www.example.com → Intermediate CA → Root CA
        //    路径中的 Root CA 证书会被验证器与信任锚进行匹配
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509", "BC");
        CertPath certPath = certFactory.generateCertPath(
                List.of(eeCert, intermediateCert, rootCert)
        );

        System.out.println("证书路径构建完成，长度: " + certPath.getCertificates().size());

        // === 4. 配置 PKIX 验证参数 ===

        // 4a. 信任锚：以 Root CA 作为信任锚
        TrustAnchor trustAnchor = new TrustAnchor(rootCert, null);
        System.out.println("信任锚: " + trustAnchor.getTrustedCert().getSubjectX500Principal().getName());

        // 4b. 构建 PKIXParameters
        PKIXParameters params = new PKIXParameters(Set.of(trustAnchor));

        // 4c. 禁用 CRL 检查（本演示不涉及 CRL 分发点配置）
        //    生产环境中应启用 revocationEnabled=true 并配置 CRL 或 OCSP
        params.setRevocationEnabled(false);

        // === 5. 执行证书路径验证 ===
        //    使用 BC provider 的 PKIX 验证器（能正确处理 BC 生成的证书签名）
        CertPathValidator validator = CertPathValidator.getInstance("PKIX", "BC");

        PKIXCertPathValidatorResult result = (PKIXCertPathValidatorResult) validator.validate(certPath, params);

        // === 6. 验证结果断言 ===
        assertNotNull(result, "路径验证结果不应为 null");
        assertNotNull(result.getTrustAnchor(), "应返回信任锚");
        assertEquals(rootCert.getSubjectX500Principal().getName(),
                result.getTrustAnchor().getTrustedCert().getSubjectX500Principal().getName(),
                "信任锚应为 Root CA");

        System.out.println("=== 证书路径验证成功 ===");
        System.out.println("证书路径: www.example.com → Intermediate CA → Root CA");
        System.out.println("信任锚: " + result.getTrustAnchor().getTrustedCert().getSubjectX500Principal().getName());
        System.out.println("验证算法: PKIX");
        System.out.println("路径中证书数: " + certPath.getCertificates().size());

        // === 7. 验证失败场景：错误的信任锚 ===
        //    使用不相关的 CA 作为信任锚，验证应失败
        KeyPair unrelatedKeyPair = generateRsaKeyPair();
        X509Certificate unrelatedCert = createRootCACert(unrelatedKeyPair);

        TrustAnchor wrongAnchor = new TrustAnchor(unrelatedCert, null);
        PKIXParameters wrongParams = new PKIXParameters(Set.of(wrongAnchor));
        wrongParams.setRevocationEnabled(false);

        CertPathValidatorException exception = assertThrows(
                CertPathValidatorException.class,
                () -> validator.validate(certPath, wrongParams),
                "使用错误信任锚时应抛出 CertPathValidatorException"
        );
        System.out.println("\n=== 错误信任锚验证（预期失败）===");
        System.out.println("异常信息: " + exception.getMessage());
    }
}
