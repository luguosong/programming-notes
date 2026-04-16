package com.luguosong.crypto;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v1CertificateBuilder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v1CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * X.509 证书生成演示
 * <p>
 * 包含 V1 自签名证书和 V3 CA 证书的创建
 */
class CertificateGenerationTest {

    @BeforeAll
    static void setup() {
        // 注册 BouncyCastle Provider，使 JCA 能够使用 BC 提供的算法实现
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

    @Test
    void shouldCreateSelfSignedCertificate() throws Exception {
        // 1. 生成 RSA 2048 密钥对
        KeyPair keyPair = generateRsaKeyPair();
        System.out.println("密钥对生成完成，算法: " + keyPair.getPublic().getAlgorithm());

        // 2. 构建 X500Name（证书主体名称）
        X500NameBuilder nameBuilder = new X500NameBuilder(BCStyle.INSTANCE);
        nameBuilder.addRDN(BCStyle.CN, "Test Root CA");
        nameBuilder.addRDN(BCStyle.O, "Test Org");
        X500Name subjectName = nameBuilder.build();

        // 3. 设置证书有效期
        Date notBefore = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000L);
        Date notAfter = new Date(System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000);

        // 4. 使用 X509v1CertificateBuilder 构建自签名证书
        //    自签名证书：签发者（issuer）和主体（subject）相同
        X509v1CertificateBuilder certBuilder = new JcaX509v1CertificateBuilder(
                subjectName,                       // 签发者 = 主体（自签名）
                BigInteger.valueOf(1),              // 序列号
                notBefore,                         // 起始时间
                notAfter,                          // 结束时间
                subjectName,                       // 主体
                keyPair.getPublic()                // 公钥
        );

        // 5. 使用 SHA256WithRSA 算法签名
        X509CertificateHolder certHolder = certBuilder.build(
                new JcaContentSignerBuilder("SHA256WithRSA")
                        .setProvider("BC")
                        .build(keyPair.getPrivate())
        );

        // 6. 转换为标准 java.security.cert.X509Certificate
        X509Certificate cert = new JcaX509CertificateConverter()
                .setProvider("BC")
                .getCertificate(certHolder);

        // 7. 验证证书：自签名证书用自身公钥验证
        cert.verify(keyPair.getPublic());

        // 8. 断言检查
        assertEquals(1, cert.getVersion(), "V1 证书版本号应为 1");
        // 注意：X500Principal.getName() 返回的 DN 顺序可能与构建顺序不同（RDN 按字典序排序）
        assertTrue(cert.getSubjectX500Principal().getName().contains("CN=Test Root CA"), "应包含 CN=Test Root CA");
        assertTrue(cert.getSubjectX500Principal().getName().contains("O=Test Org"), "应包含 O=Test Org");
        assertEquals(cert.getSubjectX500Principal().getName(),
                cert.getIssuerX500Principal().getName(), "自签名证书的主体和签发者应相同");
        assertNotNull(cert.getSerialNumber());
        assertFalse(cert.getNotBefore().after(cert.getNotAfter()), "起始时间应早于结束时间");

        System.out.println("=== V1 自签名证书创建成功 ===");
        System.out.println("版本: V" + cert.getVersion());
        System.out.println("主体: " + cert.getSubjectX500Principal().getName());
        System.out.println("签发者: " + cert.getIssuerX500Principal().getName());
        System.out.println("序列号: " + cert.getSerialNumber());
        System.out.println("有效期: " + cert.getNotBefore() + " ~ " + cert.getNotAfter());
    }

    @Test
    void shouldCreateCACertificateWithExtensions() throws Exception {
        // 1. 生成 CA 密钥对
        KeyPair caKeyPair = generateRsaKeyPair();

        // 2. 构建 CA 的 X500Name
        X500NameBuilder nameBuilder = new X500NameBuilder(BCStyle.INSTANCE);
        nameBuilder.addRDN(BCStyle.CN, "Test CA");
        nameBuilder.addRDN(BCStyle.O, "Test Org");
        nameBuilder.addRDN(BCStyle.C, "CN");
        X500Name caName = nameBuilder.build();

        // 3. 设置有效期（10 年）
        Date notBefore = new Date(System.currentTimeMillis());
        Date notAfter = new Date(System.currentTimeMillis() + 3650L * 24 * 60 * 60 * 1000);

        // 4. 使用 X509v3CertificateBuilder 构建 CA 证书
        X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                caName,                            // 自签名：签发者 = 主体
                BigInteger.valueOf(2),              // 序列号
                notBefore,
                notAfter,
                caName,
                caKeyPair.getPublic()
        );

        // 5. 添加扩展：使用 JcaX509ExtensionUtils 工具类
        JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();

        // 5a. SubjectKeyIdentifier：标识证书的公钥
        certBuilder.addExtension(
                Extension.subjectKeyIdentifier,
                false,  // non-critical
                extUtils.createSubjectKeyIdentifier(caKeyPair.getPublic())
        );

        // 5b. AuthorityKeyIdentifier：标识签发 CA 的公钥（自签名时与 SubjectKeyIdentifier 相同）
        certBuilder.addExtension(
                Extension.authorityKeyIdentifier,
                false,  // non-critical
                extUtils.createAuthorityKeyIdentifier(caKeyPair.getPublic())
        );

        // 5c. BasicConstraints：标记为 CA 证书，pathLen=0 表示不能签发下级 CA
        certBuilder.addExtension(
                Extension.basicConstraints,
                true,   // critical
                new BasicConstraints(0)  // CA=true, pathLen=0
        );

        // 5d. KeyUsage：定义公钥用途（keyCertSign: 签发证书, cRLSign: 签发 CRL）
        certBuilder.addExtension(
                Extension.keyUsage,
                true,   // critical
                new KeyUsage(
                        KeyUsage.keyCertSign | KeyUsage.cRLSign
                )
        );

        // 6. 签发证书
        X509CertificateHolder certHolder = certBuilder.build(
                new JcaContentSignerBuilder("SHA256WithRSA")
                        .setProvider("BC")
                        .build(caKeyPair.getPrivate())
        );

        // 7. 转换为 X509Certificate
        X509Certificate cert = new JcaX509CertificateConverter()
                .setProvider("BC")
                .getCertificate(certHolder);

        // 8. 验证自签名证书
        cert.verify(caKeyPair.getPublic());

        // 9. 断言检查
        assertEquals(3, cert.getVersion(), "V3 证书版本号应为 3");

        // 验证 BasicConstraints 扩展
        boolean[] keyUsage = cert.getKeyUsage();
        assertNotNull(keyUsage, "应包含 KeyUsage 扩展");
        assertTrue(keyUsage[5], "KeyUsage 应包含 keyCertSign");  // 索引 5 = keyCertSign
        assertTrue(keyUsage[6], "KeyUsage 应包含 cRLSign");      // 索引 6 = cRLSign

        // 验证 BasicConstraints
        Set<String> criticalOids = cert.getCriticalExtensionOIDs();
        assertNotNull(criticalOids, "应包含 critical 扩展");
        assertTrue(criticalOids.contains("2.5.29.19"), "BasicConstraints 应标记为 critical");

        System.out.println("=== V3 CA 证书创建成功 ===");
        System.out.println("版本: V" + cert.getVersion());
        System.out.println("主体: " + cert.getSubjectX500Principal().getName());
        System.out.println("序列号: " + cert.getSerialNumber());
        System.out.println("Critical 扩展: " + cert.getCriticalExtensionOIDs());
        System.out.println("Non-Critical 扩展: " + cert.getNonCriticalExtensionOIDs());

        // 解码并打印 BasicConstraints
        byte[] bcExt = cert.getExtensionValue("2.5.29.19");
        assertNotNull(bcExt, "BasicConstraints 扩展值不应为 null");
        System.out.println("BasicConstraints 扩展存在: " + (bcExt.length > 0));
    }
}
