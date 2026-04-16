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
 * PKCS#12 密钥库演示
 *
 * PKCS#12 是跨平台的密钥库标准格式（.p12 / .pfx），
 * 广泛用于存储私钥、证书链和信任证书。
 * JDK 9 起作为 Java 默认密钥库类型。
 */
class Pkcs12KeyStoreTest {

    @TempDir
    Path tempDir;

    @BeforeAll
    static void setup() {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * 生成 RSA 密钥对 + 自签名证书
     */
    private record KeyPairAndCert(KeyPair keyPair, X509Certificate cert) {}

    private KeyPairAndCert generateKeyPairAndCert(String cn) throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair keyPair = kpg.generateKeyPair();

        X500Name dn = new X500Name("CN=" + cn + ", O=Luguosong, C=CN");
        Date notBefore = new Date(System.currentTimeMillis() - 86400000L);
        Date notAfter = new Date(System.currentTimeMillis() + 365L * 86400000 * 1000);
        SubjectPublicKeyInfo pubKeyInfo = SubjectPublicKeyInfo.getInstance(keyPair.getPublic().getEncoded());

        X509v1CertificateBuilder builder = new X509v1CertificateBuilder(
                dn, BigInteger.valueOf(System.currentTimeMillis()),
                notBefore, notAfter, dn, pubKeyInfo);

        X509CertificateHolder holder = builder.build(
                new JcaContentSignerBuilder("SHA256WithRSA").build(keyPair.getPrivate()));

        X509Certificate cert = new JcaX509CertificateConverter().setProvider("BC")
                .getCertificate(holder);

        return new KeyPairAndCert(keyPair, cert);
    }

    @Test
    void shouldCreateAndLoadPKCS12KeyStore() throws Exception {
        // ===== 第一步：生成密钥对和证书 =====
        KeyPairAndCert kpc = generateKeyPairAndCert("Test Server");
        char[] password = "changeit".toCharArray();
        String alias = "server-key";
        File p12File = tempDir.resolve("server.p12").toFile();

        // ===== 第二步：创建 PKCS#12 密钥库 =====
        // PKCS#12 支持空密码初始化
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(null, null);

        // 存入私钥 + 证书链
        X509Certificate[] certChain = new X509Certificate[]{kpc.cert};
        ks.setKeyEntry(alias, kpc.keyPair.getPrivate(), password, certChain);

        // 写入 .p12 文件
        try (FileOutputStream fos = new FileOutputStream(p12File)) {
            ks.store(fos, password);
        }
        System.out.println("PKCS#12 密钥库已创建: " + p12File.getAbsolutePath());
        System.out.println("文件大小: " + p12File.length() + " bytes");

        // ===== 第三步：重新加载并验证 =====
        KeyStore loadedKs = KeyStore.getInstance("PKCS12");
        try (FileInputStream fis = new FileInputStream(p12File)) {
            loadedKs.load(fis, password);
        }

        // 验证私钥条目
        Key privateKey = loadedKs.getKey(alias, password);
        assertNotNull(privateKey, "私钥应该存在");
        assertEquals("RSA", privateKey.getAlgorithm());

        // 验证证书链
        Certificate[] chain = loadedKs.getCertificateChain(alias);
        assertNotNull(chain);
        assertEquals(1, chain.length);
        X509Certificate leaf = (X509Certificate) chain[0];
        // DN 的 RDN 顺序可能与创建时不同（X500Name 内部可能重排），只验证 CN 部分
        String subject = leaf.getSubjectX500Principal().getName();
        assertTrue(subject.contains("CN=Test Server"), "证书应包含 CN=Test Server");

        System.out.println("PKCS#12 加载验证通过，证书主题: " + leaf.getSubjectX500Principal().getName());
    }

    @Test
    void shouldSupportTrustedCertificateEntry() throws Exception {
        // 模拟两个不同的 CA 证书
        KeyPairAndCert ca1 = generateKeyPairAndCert("Root CA 1");
        KeyPairAndCert ca2 = generateKeyPairAndCert("Root CA 2");
        char[] password = "changeit".toCharArray();
        File p12File = tempDir.resolve("truststore.p12").toFile();

        // 创建 PKCS#12 密钥库，存储信任证书
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(null, null);

        // setCertificateEntry：存储信任证书（仅公钥，不含私钥）
        ks.setCertificateEntry("root-ca-1", ca1.cert);
        ks.setCertificateEntry("root-ca-2", ca2.cert);

        // 同时存储一个带私钥的条目
        KeyPairAndCert server = generateKeyPairAndCert("Server");
        ks.setKeyEntry("server-key", server.keyPair.getPrivate(), password,
                new X509Certificate[]{server.cert});

        try (FileOutputStream fos = new FileOutputStream(p12File)) {
            ks.store(fos, password);
        }

        // 加载并验证
        KeyStore loadedKs = KeyStore.getInstance("PKCS12");
        try (FileInputStream fis = new FileInputStream(p12File)) {
            loadedKs.load(fis, password);
        }

        // 验证信任证书
        X509Certificate trusted1 = (X509Certificate) loadedKs.getCertificate("root-ca-1");
        X509Certificate trusted2 = (X509Certificate) loadedKs.getCertificate("root-ca-2");
        assertNotNull(trusted1);
        assertNotNull(trusted2);
        assertTrue(loadedKs.isCertificateEntry("root-ca-1"), "root-ca-1 应为证书条目");
        assertTrue(loadedKs.isCertificateEntry("root-ca-2"), "root-ca-2 应为证书条目");
        assertFalse(loadedKs.isCertificateEntry("server-key"), "server-key 不应为证书条目");
        assertTrue(loadedKs.isKeyEntry("server-key"), "server-key 应为密钥条目");

        System.out.println("===== 信任证书条目 =====");
        System.out.println("  root-ca-1: " + trusted1.getSubjectX500Principal().getName());
        System.out.println("  root-ca-2: " + trusted2.getSubjectX500Principal().getName());
        System.out.println("PKCS#12 信任证书存储验证通过");
    }
}
