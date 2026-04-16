package com.luguosong.crypto;

import org.bouncycastle.bcpg.*;
import org.bouncycastle.bcpg.sig.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.PBESecretKeyEncryptor;
import org.bouncycastle.openpgp.operator.PGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.PGPDigestCalculator;
import org.bouncycastle.openpgp.jcajce.JcaPGPObjectFactory;
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
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OpenPGP 签名与加密测试
 *
 * <p>演示 PGP 消息的数字签名/验证和公钥加密/解密流程。
 * 签名使用二进制签名格式（detached signature），加密使用 AES-256。</p>
 */
class PgpSignEncryptTest {

    @TempDir
    Path tempDir;

    private static final String USER_ID = "alice@example.com";
    private static final char[] PASSPHRASE = "test-passphrase".toCharArray();

    @BeforeAll
    static void setUp() {
        // 注册 BouncyCastle Provider
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * 生成 RSA 3072 位密钥环的辅助方法
     */
    private static PGPSecretKeyRing generateKeyRing(String userId, char[] passphrase) throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA", "BC");
        kpg.initialize(3072);
        Date now = new Date();

        KeyPair primaryKP = kpg.generateKeyPair();
        PGPKeyPair primaryKey = new JcaPGPKeyPair(PGPPublicKey.RSA_GENERAL, primaryKP, now);

        KeyPair encryptKP = kpg.generateKeyPair();
        PGPKeyPair encryptKey = new JcaPGPKeyPair(PGPPublicKey.RSA_GENERAL, encryptKP, now);

        PGPSignatureSubpacketGenerator subpackets = new PGPSignatureSubpacketGenerator();
        subpackets.setKeyFlags(true, KeyFlags.CERTIFY_OTHER | KeyFlags.SIGN_DATA);
        subpackets.setPreferredHashAlgorithms(false, new int[]{
                HashAlgorithmTags.SHA512, HashAlgorithmTags.SHA384, HashAlgorithmTags.SHA256});
        subpackets.setPreferredSymmetricAlgorithms(false, new int[]{
                SymmetricKeyAlgorithmTags.AES_256, SymmetricKeyAlgorithmTags.AES_128});

        PGPDigestCalculator sha1Calc = new JcaPGPDigestCalculatorProviderBuilder()
                .build().get(HashAlgorithmTags.SHA1);
        PGPContentSignerBuilder signerBuilder = new JcaPGPContentSignerBuilder(
                PGPPublicKey.RSA_GENERAL, HashAlgorithmTags.SHA256).setProvider("BC");
        PBESecretKeyEncryptor encryptor = new JcePBESecretKeyEncryptorBuilder(
                SymmetricKeyAlgorithmTags.AES_256, sha1Calc).setProvider("BC")
                .build(passphrase);

        PGPKeyRingGenerator ringGen = new PGPKeyRingGenerator(
                PGPSignature.POSITIVE_CERTIFICATION, primaryKey,
                userId, sha1Calc, subpackets.generate(), null,
                signerBuilder, encryptor);

        PGPSignatureSubpacketGenerator encSubpackets = new PGPSignatureSubpacketGenerator();
        encSubpackets.setKeyFlags(true, KeyFlags.ENCRYPT_COMMS | KeyFlags.ENCRYPT_STORAGE);
        ringGen.addSubKey(encryptKey, encSubpackets.generate(), null);

        return ringGen.generateSecretKeyRing();
    }

    /**
     * 从公钥环中查找可用于加密的公钥
     */
    private static PGPPublicKey findEncryptionKey(PGPPublicKeyRing publicKeyRing) {
        Iterator<PGPPublicKey> it = publicKeyRing.getPublicKeys();
        while (it.hasNext()) {
            PGPPublicKey key = it.next();
            if (key.isEncryptionKey()) {
                return key;
            }
        }
        throw new RuntimeException("密钥环中未找到加密公钥");
    }

    /**
     * PGP 签名消息并验证签名（使用二进制签名格式）
     */
    @Test
    @DisplayName("PGP 签名消息并验证签名")
    void shouldSignAndVerifyMessage() throws Exception {
        // 1. 生成密钥环
        PGPSecretKeyRing secretKeyRing = generateKeyRing(USER_ID, PASSPHRASE);
        PGPPublicKeyRing publicKeyRing = secretKeyRing.toCertificate();

        // 2. 准备待签名的消息
        String message = "这是一条待签名的 PGP 消息，用于验证签名功能。";
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);

        // 3. 提取签名私钥
        PGPSecretKey signingSecretKey = secretKeyRing.getSecretKey();
        PGPPrivateKey signingPrivateKey = signingSecretKey.extractPrivateKey(
                new JcePBESecretKeyDecryptorBuilder().setProvider("BC").build(PASSPHRASE));
        PGPPublicKey signingPublicKey = signingSecretKey.getPublicKey();

        // 4. 创建签名生成器
        PGPSignatureGenerator signatureGenerator = new PGPSignatureGenerator(
                new JcaPGPContentSignerBuilder(signingPublicKey.getAlgorithm(), HashAlgorithmTags.SHA256)
                        .setProvider("BC"));

        // 5. 使用私钥初始化签名生成器，并设置签名类型为二进制文档签名
        signatureGenerator.init(PGPSignature.BINARY_DOCUMENT, signingPrivateKey);

        // 6. 更新待签名数据并生成签名
        signatureGenerator.update(messageBytes);
        byte[] signatureBytes = signatureGenerator.generate().getEncoded();

        System.out.println("签名长度: " + signatureBytes.length + " 字节");
        System.out.println("签名密钥 ID: 0x" + Long.toHexString(signingPublicKey.getKeyID()).toUpperCase());
        assertTrue(signatureBytes.length > 0, "签名数据不应为空");

        // 7. 导出签名到临时文件（ASCII-Armored 格式）
        Path sigPath = tempDir.resolve("message.sig");
        ByteArrayOutputStream sigOut = new ByteArrayOutputStream();
        try (OutputStream out = new ArmoredOutputStream(sigOut)) {
            out.write(signatureBytes);
        }
        byte[] sigData = sigOut.toByteArray();
        Files.write(sigPath, sigData);
        System.out.println("签名已导出至: " + sigPath.toAbsolutePath());

        // 8. 使用 BCPGInputStream 解析签名对象
        PGPSignature signature;
        try (InputStream sigIn = PGPUtil.getDecoderStream(new ByteArrayInputStream(sigData))) {
            BCPGInputStream bcpgIn = new BCPGInputStream(sigIn);
            PGPSignature sig = new PGPSignature(bcpgIn);
            signature = sig;
        }

        // 9. 使用公钥初始化验证
        signature.init(new JcaPGPContentVerifierBuilderProvider().setProvider("BC"),
                signingPublicKey);

        // 10. 更新原始数据并验证签名
        signature.update(messageBytes);
        assertTrue(signature.verify(), "签名验证应该通过");

        // 11. 验证：篡改消息后签名应该失败
        PGPSignature tamperedSig;
        try (InputStream sigIn = PGPUtil.getDecoderStream(new ByteArrayInputStream(sigData))) {
            BCPGInputStream bcpgIn = new BCPGInputStream(sigIn);
            tamperedSig = new PGPSignature(bcpgIn);
        }
        tamperedSig.init(new JcaPGPContentVerifierBuilderProvider().setProvider("BC"),
                signingPublicKey);
        tamperedSig.update("被篡改的消息内容".getBytes(StandardCharsets.UTF_8));
        assertFalse(tamperedSig.verify(), "篡改消息后签名验证应该失败");

        System.out.println("签名验证测试通过");
    }

    /**
     * PGP 公钥加密消息并解密（AES-256 + RSA）
     */
    @Test
    @DisplayName("PGP 加密消息并解密 - AES-256")
    void shouldEncryptAndDecryptMessage() throws Exception {
        // 1. 生成密钥环
        PGPSecretKeyRing secretKeyRing = generateKeyRing(USER_ID, PASSPHRASE);
        PGPPublicKeyRing publicKeyRing = secretKeyRing.toCertificate();

        // 2. 查找加密公钥
        PGPPublicKey encryptionPublicKey = findEncryptionKey(publicKeyRing);
        System.out.println("加密公钥 ID: 0x" + Long.toHexString(encryptionPublicKey.getKeyID()).toUpperCase());

        // 3. 准备待加密的明文消息
        String originalMessage = "这是一条需要加密的机密消息，只有持有私钥的人才能解密。";
        byte[] plaintext = originalMessage.getBytes(StandardCharsets.UTF_8);

        // ========== 加密流程 ==========

        // 4. 先压缩数据（PGP 通常先压缩再加密）
        ByteArrayOutputStream compressedOut = new ByteArrayOutputStream();
        PGPCompressedDataGenerator compressor = new PGPCompressedDataGenerator(
                CompressionAlgorithmTags.ZIP);
        try (OutputStream compStream = compressor.open(compressedOut)) {
            PGPLiteralDataGenerator literal = new PGPLiteralDataGenerator();
            try (OutputStream litStream = literal.open(compStream,
                    PGPLiteralData.BINARY, "message.txt", plaintext.length, new Date())) {
                litStream.write(plaintext);
            }
        }
        byte[] compressedData = compressedOut.toByteArray();

        // 5. 使用 AES-256 加密压缩后的数据
        ByteArrayOutputStream encryptedOut = new ByteArrayOutputStream();
        try (OutputStream armoredOut = new ArmoredOutputStream(encryptedOut)) {
            PGPEncryptedDataGenerator encGen = new PGPEncryptedDataGenerator(
                    new JcePGPDataEncryptorBuilder(SymmetricKeyAlgorithmTags.AES_256)
                            .setWithIntegrityPacket(true)
                            .setSecureRandom(new SecureRandom())
                            .setProvider("BC"));
            encGen.addMethod(new JcePublicKeyKeyEncryptionMethodGenerator(encryptionPublicKey)
                    .setProvider("BC"));

            try (OutputStream encStream = encGen.open(armoredOut, compressedData.length)) {
                encStream.write(compressedData);
            }
        }
        byte[] encryptedData = encryptedOut.toByteArray();

        // 6. 打印加密消息的前几行
        System.out.println("加密消息长度: " + encryptedData.length + " 字节");
        System.out.println("--- 加密消息（前 5 行）---");
        String encryptedStr = encryptedOut.toString(StandardCharsets.UTF_8);
        String[] lines = encryptedStr.split("\n");
        for (int i = 0; i < Math.min(5, lines.length); i++) {
            System.out.println(lines[i]);
        }
        System.out.println("...");

        // 导出加密消息到临时文件
        Path encryptedPath = tempDir.resolve("encrypted-message.asc");
        Files.write(encryptedPath, encryptedData);
        System.out.println("加密消息已导出至: " + encryptedPath.toAbsolutePath());

        // ========== 解密流程 ==========

        // 7. 读取加密消息，解析 PGP 加密数据列表
        PGPPublicKeyEncryptedData pke;
        try (InputStream encIn = PGPUtil.getDecoderStream(new ByteArrayInputStream(encryptedData))) {
            InputStream pgpIn = encIn;
            // 跳过可能的压缩层，找到加密数据包
            Object obj = pgpIn.read();
            while (obj instanceof InputStream || obj instanceof byte[]) {
                // 如果是压缩数据包装的，继续读取内部数据
                if (obj instanceof InputStream wrappedStream) {
                    obj = wrappedStream.read();
                } else {
                    break;
                }
            }

            // 使用 PGPObjectFactory 解析 PGP 数据包
            InputStream objFactoryIn = PGPUtil.getDecoderStream(new ByteArrayInputStream(encryptedData));
            PGPObjectFactory pgpFactory = new JcaPGPObjectFactory(objFactoryIn);
            Object firstObj = pgpFactory.nextObject();

            // PGP 加密数据可能嵌套在压缩包中
            PGPEncryptedDataList encList;
            if (firstObj instanceof PGPEncryptedDataList) {
                encList = (PGPEncryptedDataList) firstObj;
            } else if (firstObj instanceof PGPCompressedData) {
                // 解压缩后获取加密数据
                PGPObjectFactory innerFactory = new JcaPGPObjectFactory(
                        ((PGPCompressedData) firstObj).getDataStream());
                encList = (PGPEncryptedDataList) innerFactory.nextObject();
            } else {
                throw new RuntimeException("未预期的 PGP 数据包类型: " + firstObj.getClass().getName());
            }

            pke = (PGPPublicKeyEncryptedData) encList.get(0);
        }

        // 8. 查找用于解密的私钥（通过密钥 ID 匹配）
        PGPSecretKey decryptSecretKey = findSecretKeyForPublicKey(secretKeyRing, pke.getKeyID());
        PGPPrivateKey decryptPrivateKey = decryptSecretKey.extractPrivateKey(
                new JcePBESecretKeyDecryptorBuilder().setProvider("BC").build(PASSPHRASE));

        // 9. 使用私钥解密，获取内部数据流
        InputStream clearStream = pke.getDataStream(
                new JcePublicKeyDataDecryptorFactoryBuilder().setProvider("BC")
                        .build(decryptPrivateKey));

        // 10. 使用 PGPObjectFactory 解析解密后的数据（先解压缩再读取明文）
        PGPObjectFactory clearFactory = new JcaPGPObjectFactory(clearStream);
        Object clearObj = clearFactory.nextObject();

        // 如果是压缩数据，先解压缩
        if (clearObj instanceof PGPCompressedData compressedDataObj) {
            PGPObjectFactory litFactory = new JcaPGPObjectFactory(
                    compressedDataObj.getDataStream());
            clearObj = litFactory.nextObject();
        }

        // 11. 读取解密后的明文
        PGPLiteralData literalData = (PGPLiteralData) clearObj;
        ByteArrayOutputStream decryptedOut = new ByteArrayOutputStream();
        try (InputStream plainIn = literalData.getInputStream()) {
            plainIn.transferTo(decryptedOut);
        }
        String decryptedMessage = decryptedOut.toString(StandardCharsets.UTF_8);

        System.out.println("解密后的消息: " + decryptedMessage);

        // 12. 验证解密后的消息与原始消息一致
        assertEquals(originalMessage, decryptedMessage,
                "解密后的消息应与原始消息一致");

        // 13. 验证完整性保护（MDC - Modification Detection Code）
        assertTrue(pke.verify(),
                "MDC 完整性校验应该通过");

        System.out.println("加密解密测试通过");
    }

    /**
     * 在私钥环中查找与给定公钥 ID 对应的私钥
     */
    private static PGPSecretKey findSecretKeyForPublicKey(PGPSecretKeyRing secretKeyRing, long keyId) {
        Iterator<PGPSecretKey> it = secretKeyRing.getSecretKeys();
        while (it.hasNext()) {
            PGPSecretKey secretKey = it.next();
            if (secretKey.getKeyID() == keyId) {
                return secretKey;
            }
        }
        // 如果没有精确匹配，尝试通过子密钥 ID 匹配
        it = secretKeyRing.getSecretKeys();
        while (it.hasNext()) {
            PGPSecretKey secretKey = it.next();
            if (secretKey.getPublicKey().getKeyID() == keyId) {
                return secretKey;
            }
        }
        throw new RuntimeException("私钥环中未找到匹配密钥 ID: 0x" + Long.toHexString(keyId));
    }
}
