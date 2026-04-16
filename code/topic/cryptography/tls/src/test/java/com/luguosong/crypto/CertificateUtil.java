package com.luguosong.crypto;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * 证书生成工具类
 * <p>
 * 基于 Bouncy Castle 提供自签名证书生成能力，
 * 供 TLS 上下文和握手测试复用。
 */
public class CertificateUtil {

    private CertificateUtil() {
        // 工具类禁止实例化
    }

    /**
     * 生成 RSA 密钥对
     *
     * @param keySize 密钥长度（推荐 2048 或 4096）
     * @return RSA 密钥对
     */
    public static KeyPair generateRsaKeyPair(int keySize) throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", new BouncyCastleProvider());
        generator.initialize(keySize, new SecureRandom());
        return generator.generateKeyPair();
    }

    /**
     * 生成自签名 X.509 证书（v3）
     *
     * @param keyPair   密钥对（公钥嵌入证书，私钥用于签名）
     * @param cn        Common Name（证书主体名称）
     * @param daysValid 证书有效期（天）
     * @return 自签名 X.509 证书
     */
    public static X509Certificate generateSelfSignedCertificate(
            KeyPair keyPair, String cn, int daysValid) throws Exception {

        // 设置证书有效期（从当前时间开始）
        Instant now = Instant.now();
        Date notBefore = Date.from(now);
        Date notAfter = Date.from(now.plus(daysValid, ChronoUnit.DAYS));

        // 证书序列号（使用随机数保证唯一性）
        BigInteger serialNumber = new BigInteger(64, new SecureRandom());

        // 证书主体和颁发者（自签名，两者相同）
        X500Name issuer = new X500Name("CN=" + cn);
        X500Name subject = new X500Name("CN=" + cn);

        // 从公钥构建 SubjectPublicKeyInfo
        SubjectPublicKeyInfo publicKeyInfo = SubjectPublicKeyInfo.getInstance(
                keyPair.getPublic().getEncoded());

        // 使用 X509v3CertificateBuilder 构建证书
        X509v3CertificateBuilder certBuilder = new X509v3CertificateBuilder(
                issuer,               // 颁发者
                serialNumber,         // 序列号
                notBefore,            // 生效时间
                notAfter,             // 过期时间
                subject,              // 主体
                publicKeyInfo         // 公钥信息
        );

        // 扩展工具类（用于计算 SubjectKeyIdentifier）
        JcaX509ExtensionUtils extensionUtils = new JcaX509ExtensionUtils();

        // 添加标准扩展
        certBuilder.addExtension(
                org.bouncycastle.asn1.x509.Extension.subjectKeyIdentifier,
                false,
                extensionUtils.createSubjectKeyIdentifier(keyPair.getPublic())
        );
        certBuilder.addExtension(
                org.bouncycastle.asn1.x509.Extension.authorityKeyIdentifier,
                false,
                extensionUtils.createAuthorityKeyIdentifier(keyPair.getPublic())
        );
        certBuilder.addExtension(
                org.bouncycastle.asn1.x509.Extension.basicConstraints,
                true,
                new BasicConstraints(true)  // CA 证书
        );
        certBuilder.addExtension(
                org.bouncycastle.asn1.x509.Extension.keyUsage,
                true,
                new KeyUsage(
                        KeyUsage.digitalSignature |
                        KeyUsage.keyEncipherment |
                        KeyUsage.keyCertSign |
                        KeyUsage.cRLSign
                )
        );
        certBuilder.addExtension(
                org.bouncycastle.asn1.x509.Extension.extendedKeyUsage,
                false,
                new ExtendedKeyUsage(
                        new KeyPurposeId[]{
                                KeyPurposeId.id_kp_serverAuth,
                                KeyPurposeId.id_kp_clientAuth
                        }
                )
        );

        // 使用私钥对证书进行签名（SHA256withRSA）
        ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA")
                .setProvider(new BouncyCastleProvider())
                .build(keyPair.getPrivate());

        // 生成证书持有者对象
        X509CertificateHolder certHolder = certBuilder.build(signer);

        // 转换为 JDK 标准 X509Certificate 对象
        return new JcaX509CertificateConverter()
                .setProvider(new BouncyCastleProvider())
                .getCertificate(certHolder);
    }

    /**
     * 生成完整的 TLS 密钥材料（密钥对 + 自签名证书）
     *
     * @param cn 证书 Common Name
     * @return 包含密钥对和证书的数组 [KeyPair, X509Certificate]
     */
    public static Object[] generateKeyMaterial(String cn) throws Exception {
        KeyPair keyPair = generateRsaKeyPair(2048);
        X509Certificate cert = generateSelfSignedCertificate(keyPair, cn, 365);
        return new Object[]{keyPair, cert};
    }

    /**
     * 获取 BouncyCastle Provider 实例
     *
     * @return BouncyCastle Provider
     */
    public static BouncyCastleProvider getBouncyCastleProvider() {
        return new BouncyCastleProvider();
    }
}
