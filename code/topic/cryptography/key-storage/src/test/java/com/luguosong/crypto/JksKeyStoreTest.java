package com.luguosong.crypto;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v1CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JKS（Java KeyStore）密钥库演示
 *
 * JKS 是 Java 专有的密钥库格式，可存储私钥和证书链。
 * JDK 9 起默认密钥库类型改为 PKCS12，JKS 仍可使用但已不推荐用于生产环境。
 */
class JksKeyStoreTest {

    @TempDir
    Path tempDir;

    @BeforeAll
    static void setup() {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * 生成 RSA 密钥对 + 自签名证书
     */
    private KeyPairAndCert generateKeyPairAndCert() throws Exception {
        // 生成 2048 位 RSA 密钥对
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair keyPair = kpg.generateKeyPair();

        // 使用 BC 的 X509v1CertificateBuilder 创建自签名证书
        X500Name issuer = new X500Name("CN=Test, O=Luguosong, C=CN");
        X500Name subject = issuer; // 自签名：颁发者 = 主体
        Date notBefore = new Date(System.currentTimeMillis() - 86400000L); // 昨天
        Date notAfter = new Date(System.currentTimeMillis() + 365L * 86400000 * 1000); // 一年后

        // 从公钥提取 SubjectPublicKeyInfo
        SubjectPublicKeyInfo pubKeyInfo = SubjectPublicKeyInfo.getInstance(keyPair.getPublic().getEncoded());

        // 构建证书（序列号随机，签名算法 SHA256WithRSA）
        X509v1CertificateBuilder certBuilder = new X509v1CertificateBuilder(
                issuer, BigInteger.valueOf(System.currentTimeMillis()),
                notBefore, notAfter, subject, pubKeyInfo);

        // 用私钥签名
        X509CertificateHolder certHolder = certBuilder.build(
                new JcaContentSignerBuilder("SHA256WithRSA").build(keyPair.getPrivate()));

        // 转为 JDK 标准的 X509Certificate 对象
        X509Certificate cert = new JcaX509CertificateConverter().setProvider("BC")
                .getCertificate(certHolder);

        return new KeyPairAndCert(keyPair, cert);
    }

    /** 密钥对 + 证书的封装 */
    private record KeyPairAndCert(KeyPair keyPair, X509Certificate cert) {}

    @Test
    void shouldCreateAndLoadJKSKeyStore() throws Exception {
        // ===== 第一步：生成密钥对和自签名证书 =====
        KeyPairAndCert kpc = generateKeyPairAndCert();
        char[] password = "changeit".toCharArray();
        String alias = "my-key";
        File ksFile = tempDir.resolve("test.jks").toFile();

        // ===== 第二步：创建 JKS 密钥库并存储条目 =====
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(null, null); // 初始化空密钥库

        // 存入私钥（需要关联证书链）
        X509Certificate[] certChain = new X509Certificate[]{kpc.cert};
        ks.setKeyEntry(alias, kpc.keyPair.getPrivate(), password, certChain);

        // 同时存入一个信任证书条目
        ks.setCertificateEntry("trusted-root", kpc.cert);

        // 写入文件
        try (FileOutputStream fos = new FileOutputStream(ksFile)) {
            ks.store(fos, password);
        }
        System.out.println("JKS 密钥库已创建: " + ksFile.getAbsolutePath());

        // ===== 第三步：重新加载并验证 =====
        KeyStore loadedKs = KeyStore.getInstance("JKS");
        try (FileInputStream fis = new FileInputStream(ksFile)) {
            loadedKs.load(fis, password);
        }

        // 验证私钥条目
        Key loadedKey = loadedKs.getKey(alias, password);
        assertNotNull(loadedKey, "私钥应该存在");
        Certificate[] loadedChain = loadedKs.getCertificateChain(alias);
        assertNotNull(loadedChain, "证书链应该存在");
        assertEquals(1, loadedChain.length);

        // 验证信任证书条目
        Certificate trustedCert = loadedKs.getCertificate("trusted-root");
        assertNotNull(trustedCert, "信任证书应该存在");

        System.out.println("JKS 加载验证通过，私钥类型: " + loadedKey.getAlgorithm());
    }

    @Test
    void shouldListKeyStoreEntries() throws Exception {
        KeyPairAndCert kpc = generateKeyPairAndCert();
        char[] password = "changeit".toCharArray();
        File ksFile = tempDir.resolve("list-test.jks").toFile();

        // 创建密钥库，存入多个条目
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(null, null);

        ks.setKeyEntry("rsa-key-1", kpc.keyPair.getPrivate(), password,
                new X509Certificate[]{kpc.cert});

        // 再生成一对密钥，存第二个条目
        KeyPairAndCert kpc2 = generateKeyPairAndCert();
        ks.setKeyEntry("rsa-key-2", kpc2.keyPair.getPrivate(), password,
                new X509Certificate[]{kpc2.cert});

        ks.setCertificateEntry("ca-cert", kpc.cert);

        try (FileOutputStream fos = new FileOutputStream(ksFile)) {
            ks.store(fos, password);
        }

        // 重新加载后列出所有条目
        KeyStore loadedKs = KeyStore.getInstance("JKS");
        try (FileInputStream fis = new FileInputStream(ksFile)) {
            loadedKs.load(fis, password);
        }

        // 遍历密钥库中的所有别名
        System.out.println("===== JKS 密钥库条目列表 =====");
        java.util.Enumeration<String> aliases = loadedKs.aliases();
        int keyEntryCount = 0;
        int certEntryCount = 0;
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            boolean isKeyEntry = loadedKs.isKeyEntry(alias);
            boolean isCertEntry = loadedKs.isCertificateEntry(alias);
            if (isKeyEntry) keyEntryCount++;
            if (isCertEntry) certEntryCount++;

            String type = isKeyEntry ? "KeyEntry" : (isCertEntry ? "CertEntry" : "Unknown");
            Certificate cert = loadedKs.getCertificate(alias);
            String subject = "N/A";
            if (cert instanceof X509Certificate x509) {
                subject = x509.getSubjectX500Principal().getName();
            }
            System.out.println("  [" + type + "] " + alias + " -> " + subject);
        }

        assertEquals(2, keyEntryCount, "应有 2 个私钥条目");
        assertEquals(1, certEntryCount, "应有 1 个证书条目");
    }
}
