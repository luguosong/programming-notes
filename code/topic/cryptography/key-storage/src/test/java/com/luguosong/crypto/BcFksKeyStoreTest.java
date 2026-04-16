package com.luguosong.crypto;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v1CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.crypto.util.PBKDFConfig;
import org.bouncycastle.crypto.util.ScryptConfig;
import org.bouncycastle.jcajce.BCFKSLoadStoreParameter;
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
 * BCFKS（Bouncy Castle FIPS KeyStore）密钥库演示
 *
 * BCFKS 是 Bouncy Castle 提供的专有密钥库格式，具有以下特点：
 * - 支持多种密钥保护算法（AES-CMAC / Scrypt / HMAC-SHA256）
 * - FIPS 兼容设计（配合 BC-FJA 模块使用）
 * - 比 JKS/PKCS12 提供更细粒度的安全配置
 */
class BcFksKeyStoreTest {

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
    void shouldCreateAndLoadBCFKSKeyStore() throws Exception {
        KeyPairAndCert kpc = generateKeyPairAndCert("BCFKS Test");
        char[] password = "changeit".toCharArray();
        String alias = "my-key";
        File bcfsFile = tempDir.resolve("test.bcfks").toFile();

        // ===== 第一步：创建 BCFKS 密钥库 =====
        // BCFKS 是 BouncyCastle 提供的密钥库类型
        KeyStore ks = KeyStore.getInstance("BCFKS", "BC");
        ks.load(null, null); // 初始化空密钥库

        // 存入私钥 + 证书链
        X509Certificate[] certChain = new X509Certificate[]{kpc.cert};
        ks.setKeyEntry(alias, kpc.keyPair.getPrivate(), password, certChain);

        // 存入信任证书
        ks.setCertificateEntry("trusted-cert", kpc.cert);

        // ===== 第二步：使用 BCFKSLoadStoreParameter 保存 =====
        try (OutputStream out = new FileOutputStream(bcfsFile)) {
            BCFKSLoadStoreParameter storeParam = new BCFKSLoadStoreParameter.Builder(out, password)
                    .build();
            ks.store(storeParam);
        }
        System.out.println("BCFKS 密钥库已创建: " + bcfsFile.getAbsolutePath());
        System.out.println("文件大小: " + bcfsFile.length() + " bytes");

        // ===== 第三步：重新加载并验证 =====
        KeyStore loadedKs = KeyStore.getInstance("BCFKS", "BC");
        try (InputStream in = new FileInputStream(bcfsFile)) {
            BCFKSLoadStoreParameter loadParam = new BCFKSLoadStoreParameter.Builder(in, password)
                    .build();
            loadedKs.load(loadParam);
        }

        // 验证私钥
        Key privateKey = loadedKs.getKey(alias, password);
        assertNotNull(privateKey, "私钥应该存在");
        assertEquals("RSA", privateKey.getAlgorithm());

        // 验证证书链
        Certificate[] chain = loadedKs.getCertificateChain(alias);
        assertNotNull(chain);
        assertEquals(1, chain.length);

        // 验证信任证书
        Certificate trustedCert = loadedKs.getCertificate("trusted-cert");
        assertNotNull(trustedCert, "信任证书应该存在");

        System.out.println("BCFKS 加载验证通过，私钥算法: " + privateKey.getAlgorithm());
    }

    @Test
    void shouldUseScryptProtection() throws Exception {
        KeyPairAndCert kpc = generateKeyPairAndCert("Scrypt Protected");
        char[] password = "strong-password".toCharArray();
        File scryptFile = tempDir.resolve("scrypt-protected.bcfks").toFile();

        // 创建 BCFKS 密钥库
        KeyStore ks = KeyStore.getInstance("BCFKS", "BC");
        ks.load(null, null);

        // 存入密钥
        ks.setKeyEntry("scrypt-key", kpc.keyPair.getPrivate(), password,
                new X509Certificate[]{kpc.cert});

        // ===== 使用 Scrypt 保护算法保存 =====
        // Scrypt 是内存密集型 KDF，比 PBKDF2 更抗 GPU/ASIC 暴力破解
        // 参数：N（CPU/内存成本）= 65536, r（块大小）= 8, p（并行因子）= 1
        PBKDFConfig scryptConfig = new ScryptConfig.Builder(65536, 8, 1).build();

        long startTime = System.currentTimeMillis();
        try (OutputStream out = new FileOutputStream(scryptFile)) {
            BCFKSLoadStoreParameter storeParam = new BCFKSLoadStoreParameter.Builder(out, password)
                    .withStorePBKDFConfig(scryptConfig)
                    .build();
            ks.store(storeParam);
        }
        long storeDuration = System.currentTimeMillis() - startTime;
        System.out.println("BCFKS + Scrypt 保存耗时: " + storeDuration + "ms");
        System.out.println("文件大小: " + scryptFile.length() + " bytes");

        // 重新加载验证（需要提供相同的 PBKDFConfig，否则参数不匹配）
        KeyStore loadedKs = KeyStore.getInstance("BCFKS", "BC");
        startTime = System.currentTimeMillis();
        try (InputStream in = new FileInputStream(scryptFile)) {
            BCFKSLoadStoreParameter loadParam = new BCFKSLoadStoreParameter.Builder(in, password)
                    .withStorePBKDFConfig(scryptConfig)
                    .build();
            loadedKs.load(loadParam);
        }
        long loadDuration = System.currentTimeMillis() - startTime;
        System.out.println("BCFKS + Scrypt 加载耗时: " + loadDuration + "ms");

        // 验证密钥可正确读取
        Key key = loadedKs.getKey("scrypt-key", password);
        assertNotNull(key, "使用 Scrypt 保护后，密钥应仍可正确读取");
        assertEquals("RSA", key.getAlgorithm());

        System.out.println("BCFKS + Scrypt 保护验证通过");
    }
}
