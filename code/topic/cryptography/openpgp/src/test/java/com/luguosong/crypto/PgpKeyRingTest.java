package com.luguosong.crypto;

import org.bouncycastle.bcpg.*;
import org.bouncycastle.bcpg.sig.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.PBESecretKeyEncryptor;
import org.bouncycastle.openpgp.operator.PGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.PGPDigestCalculator;
import org.bouncycastle.openpgp.operator.jcajce.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OpenPGP 密钥环生成、导出与导入测试
 *
 * <p>演示 RSA 密钥对的生成、密钥环（SecretKeyRing / PublicKeyRing）的构建，
 * 以及公钥的 ASCII-Armored 格式导出和重新导入。</p>
 */
class PgpKeyRingTest {

    @TempDir
    Path tempDir;

    @BeforeAll
    static void setUp() {
        // 注册 BouncyCastle Provider
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * 生成 RSA 3072 位密钥环（公钥环 + 私钥环），包含主密钥（签名/认证）和加密子密钥
     */
    @Test
    @DisplayName("生成 RSA 密钥环 - 主密钥（签名+认证）+ 加密子密钥")
    void shouldGenerateRSAKeyRing() throws Exception {
        // 1. 生成 RSA 3072 位密钥对
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA", "BC");
        kpg.initialize(3072);
        Date now = new Date();

        // 2. 创建主密钥（用于签名和认证）
        KeyPair primaryKP = kpg.generateKeyPair();
        PGPKeyPair primaryKey = new JcaPGPKeyPair(PGPPublicKey.RSA_GENERAL, primaryKP, now);

        // 3. 创建加密子密钥
        KeyPair encryptKP = kpg.generateKeyPair();
        PGPKeyPair encryptKey = new JcaPGPKeyPair(PGPPublicKey.RSA_GENERAL, encryptKP, now);

        // 4. 配置主密钥的签名子包（设置密钥用途、偏好算法）
        PGPSignatureSubpacketGenerator subpackets = new PGPSignatureSubpacketGenerator();
        subpackets.setKeyFlags(true, KeyFlags.CERTIFY_OTHER | KeyFlags.SIGN_DATA);
        subpackets.setPreferredHashAlgorithms(false, new int[]{
                HashAlgorithmTags.SHA512, HashAlgorithmTags.SHA384, HashAlgorithmTags.SHA256});
        subpackets.setPreferredSymmetricAlgorithms(false, new int[]{
                SymmetricKeyAlgorithmTags.AES_256, SymmetricKeyAlgorithmTags.AES_128});

        // 5. 配置密钥环生成器所需的辅助对象
        PGPDigestCalculator sha1Calc = new JcaPGPDigestCalculatorProviderBuilder()
                .build().get(HashAlgorithmTags.SHA1);
        PGPContentSignerBuilder signerBuilder = new JcaPGPContentSignerBuilder(
                PGPPublicKey.RSA_GENERAL, HashAlgorithmTags.SHA256).setProvider("BC");
        PBESecretKeyEncryptor encryptor = new JcePBESecretKeyEncryptorBuilder(
                SymmetricKeyAlgorithmTags.AES_256, sha1Calc).setProvider("BC")
                .build("test-passphrase".toCharArray());

        // 6. 创建密钥环生成器并设置主密钥
        PGPKeyRingGenerator ringGen = new PGPKeyRingGenerator(
                PGPSignature.POSITIVE_CERTIFICATION, primaryKey,
                "alice@example.com", sha1Calc, subpackets.generate(), null,
                signerBuilder, encryptor);

        // 7. 添加加密子密钥
        PGPSignatureSubpacketGenerator encSubpackets = new PGPSignatureSubpacketGenerator();
        encSubpackets.setKeyFlags(true, KeyFlags.ENCRYPT_COMMS | KeyFlags.ENCRYPT_STORAGE);
        ringGen.addSubKey(encryptKey, encSubpackets.generate(), null);

        // 8. 生成私钥环，再从私钥环导出公钥环
        PGPSecretKeyRing secretKeyRing = ringGen.generateSecretKeyRing();
        PGPPublicKeyRing publicKeyRing = secretKeyRing.toCertificate();

        // 9. 验证密钥环基本信息
        assertNotNull(secretKeyRing, "私钥环不应为空");
        assertNotNull(publicKeyRing, "公钥环不应为空");
        assertEquals("alice@example.com", secretKeyRing.getSecretKey().getUserIDs().next(),
                "用户 ID 应为 alice@example.com");

        // 10. 验证密钥环包含主密钥和子密钥（共 2 个密钥）
        long masterKeyId = secretKeyRing.getSecretKey().getKeyID();
        System.out.println("主密钥 ID: 0x" + Long.toHexString(masterKeyId).toUpperCase());

        // 统计密钥环中的密钥数量
        int secretKeyCount = 0;
        int publicKeyCount = 0;
        java.util.Iterator<PGPSecretKey> secIt = secretKeyRing.getSecretKeys();
        while (secIt.hasNext()) { secIt.next(); secretKeyCount++; }
        java.util.Iterator<PGPPublicKey> pubIt = publicKeyRing.getPublicKeys();
        while (pubIt.hasNext()) { pubIt.next(); publicKeyCount++; }
        System.out.println("私钥环密钥数量: " + secretKeyCount);
        System.out.println("公钥环密钥数量: " + publicKeyCount);
        assertEquals(2, secretKeyCount, "私钥环应包含主密钥和加密子密钥");
        assertEquals(2, publicKeyCount, "公钥环应包含主密钥和加密子密钥");

        // 11. 导出私钥环到临时文件（ASCII-Armored 格式）
        Path secretKeyPath = tempDir.resolve("secret-key.asc");
        ByteArrayOutputStream secretKeyOut = new ByteArrayOutputStream();
        try (OutputStream out = new ArmoredOutputStream(secretKeyOut)) {
            secretKeyRing.encode(out);
        }
        Files.write(secretKeyPath, secretKeyOut.toByteArray());
        System.out.println("私钥环已导出至: " + secretKeyPath.toAbsolutePath());
        assertTrue(Files.size(secretKeyPath) > 0, "私钥环文件不应为空");
    }

    /**
     * 导出公钥为 ASCII-Armored 格式，再导入后验证一致性
     */
    @Test
    @DisplayName("导出公钥为 ASCII-Armored 格式并重新导入验证")
    void shouldExportAndImportPublicKey() throws Exception {
        // 1. 先生成密钥环（复用生成逻辑）
        PGPSecretKeyRing secretKeyRing = generateTestKeyRing("bob@example.com", "bob-passphrase");

        // 2. 从私钥环导出公钥环
        PGPPublicKeyRing originalPublicKeyRing = secretKeyRing.toCertificate();
        long originalKeyId = originalPublicKeyRing.getPublicKey().getKeyID();
        System.out.println("原始公钥 ID: 0x" + Long.toHexString(originalKeyId).toUpperCase());

        // 3. 导出公钥环为 ASCII-Armored 格式
        Path publicKeyPath = tempDir.resolve("public-key.asc");
        ByteArrayOutputStream pubKeyOut = new ByteArrayOutputStream();
        try (OutputStream out = new ArmoredOutputStream(pubKeyOut)) {
            originalPublicKeyRing.encode(out);
        }
        byte[] pubKeyBytes = pubKeyOut.toByteArray();
        Files.write(publicKeyPath, pubKeyBytes);
        System.out.println("公钥已导出至: " + publicKeyPath.toAbsolutePath());

        // 4. 打印 ASCII-Armored 公钥的前几行
        String pubKeyStr = pubKeyOut.toString(StandardCharsets.UTF_8);
        System.out.println("--- 公钥内容（前 5 行）---");
        String[] pubKeyLines = pubKeyStr.split("\n");
        for (int i = 0; i < Math.min(5, pubKeyLines.length); i++) {
            System.out.println(pubKeyLines[i]);
        }
        System.out.println("...");

        // 5. 重新导入公钥文件
        PGPPublicKeyRingCollection importedKeyRings;
        try (InputStream keyIn = new ByteArrayInputStream(pubKeyBytes)) {
            importedKeyRings = new PGPPublicKeyRingCollection(
                    PGPUtil.getDecoderStream(keyIn),
                    new JcaKeyFingerprintCalculator());
        }

        // 6. 验证导入后的公钥环
        PGPPublicKeyRing importedKeyRing = importedKeyRings.getKeyRings()
                .next();
        long importedKeyId = importedKeyRing.getPublicKey().getKeyID();
        System.out.println("导入后公钥 ID: 0x" + Long.toHexString(importedKeyId).toUpperCase());

        // 7. 验证密钥 ID 一致
        assertEquals(originalKeyId, importedKeyId,
                "导入前后的公钥密钥 ID 应一致");

        // 8. 验证用户 ID 一致
        assertEquals("bob@example.com", importedKeyRing.getPublicKey().getUserIDs().next(),
                "导入前后用户 ID 应一致");
    }

    // ===== 辅助方法：生成测试用密钥环 =====

    /**
     * 生成一个包含主密钥和加密子密钥的测试密钥环
     *
     * @param userId    用户 ID（如 "alice@example.com"）
     * @param passphrase 保护私钥的口令
     * @return 生成的私钥环
     */
    private static PGPSecretKeyRing generateTestKeyRing(String userId, String passphrase) throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA", "BC");
        kpg.initialize(3072);
        Date now = new Date();

        // 主密钥（签名 + 认证）
        KeyPair primaryKP = kpg.generateKeyPair();
        PGPKeyPair primaryKey = new JcaPGPKeyPair(PGPPublicKey.RSA_GENERAL, primaryKP, now);

        // 加密子密钥
        KeyPair encryptKP = kpg.generateKeyPair();
        PGPKeyPair encryptKey = new JcaPGPKeyPair(PGPPublicKey.RSA_GENERAL, encryptKP, now);

        // 主密钥签名子包
        PGPSignatureSubpacketGenerator subpackets = new PGPSignatureSubpacketGenerator();
        subpackets.setKeyFlags(true, KeyFlags.CERTIFY_OTHER | KeyFlags.SIGN_DATA);
        subpackets.setPreferredHashAlgorithms(false, new int[]{
                HashAlgorithmTags.SHA512, HashAlgorithmTags.SHA384, HashAlgorithmTags.SHA256});
        subpackets.setPreferredSymmetricAlgorithms(false, new int[]{
                SymmetricKeyAlgorithmTags.AES_256, SymmetricKeyAlgorithmTags.AES_128});

        // 密钥环生成器
        PGPDigestCalculator sha1Calc = new JcaPGPDigestCalculatorProviderBuilder()
                .build().get(HashAlgorithmTags.SHA1);
        PGPContentSignerBuilder signerBuilder = new JcaPGPContentSignerBuilder(
                PGPPublicKey.RSA_GENERAL, HashAlgorithmTags.SHA256).setProvider("BC");
        PBESecretKeyEncryptor encryptor = new JcePBESecretKeyEncryptorBuilder(
                SymmetricKeyAlgorithmTags.AES_256, sha1Calc).setProvider("BC")
                .build(passphrase.toCharArray());

        PGPKeyRingGenerator ringGen = new PGPKeyRingGenerator(
                PGPSignature.POSITIVE_CERTIFICATION, primaryKey,
                userId, sha1Calc, subpackets.generate(), null,
                signerBuilder, encryptor);

        // 添加加密子密钥
        PGPSignatureSubpacketGenerator encSubpackets = new PGPSignatureSubpacketGenerator();
        encSubpackets.setKeyFlags(true, KeyFlags.ENCRYPT_COMMS | KeyFlags.ENCRYPT_STORAGE);
        ringGen.addSubKey(encryptKey, encSubpackets.generate(), null);

        return ringGen.generateSecretKeyRing();
    }
}
