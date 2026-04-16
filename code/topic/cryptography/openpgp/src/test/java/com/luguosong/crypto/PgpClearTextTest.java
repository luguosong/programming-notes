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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OpenPGP 清文签名（Clear-signed）测试
 *
 * <p>清文签名是 PGP 的一种特殊签名格式，签名后的文本仍然可读，
 * 签名数据以 ASCII-Armored 格式附加在消息底部，以 "-----BEGIN PGP SIGNATURE-----" 开头。</p>
 */
class PgpClearTextTest {

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
     * 清文签名（Clear-signed）：文本保持可读，签名附加在底部
     */
    @Test
    @DisplayName("PGP 清文签名 - 文本可读 + 签名附加在底部")
    void shouldSignClearTextMessage() throws Exception {
        // 1. 生成密钥环
        PGPSecretKeyRing secretKeyRing = generateKeyRing(USER_ID, PASSPHRASE);
        PGPPublicKeyRing publicKeyRing = secretKeyRing.toCertificate();

        // 2. 准备待签名的文本消息
        String originalMessage = "这是一条清文签名消息。\n"
                + "签名后的文本仍然保持可读性，\n"
                + "PGP 签名数据会以 ASCII 格式附加在底部。";
        byte[] crlf = "\r\n".getBytes(StandardCharsets.UTF_8);

        // ========== 签名流程 ==========

        // 3. 提取签名私钥
        PGPSecretKey signingSecretKey = secretKeyRing.getSecretKey();
        PGPPrivateKey signingPrivateKey = signingSecretKey.extractPrivateKey(
                new JcePBESecretKeyDecryptorBuilder().setProvider("BC").build(PASSPHRASE));
        PGPPublicKey signingPublicKey = signingSecretKey.getPublicKey();

        // 4. 创建清文签名输出
        ByteArrayOutputStream clearSignedOut = new ByteArrayOutputStream();
        try (ArmoredOutputStream armoredOut = new ArmoredOutputStream(clearSignedOut)) {
            // 创建签名生成器
            PGPSignatureGenerator signatureGenerator = new PGPSignatureGenerator(
                    new JcaPGPContentSignerBuilder(
                            signingPublicKey.getAlgorithm(), HashAlgorithmTags.SHA256)
                            .setProvider("BC"));

            // 初始化签名
            signatureGenerator.init(PGPSignature.CANONICAL_TEXT_DOCUMENT, signingPrivateKey);

            // 5. 写入清文签名头部（Hash Armor Header）
            armoredOut.beginClearText(HashAlgorithmTags.SHA256);

            // 6. 逐行写入文本并更新签名
            //    清文签名使用规范文本格式（行尾使用 CR+LF，末行也需要 CR+LF）
            String[] msgLines = originalMessage.split("\n");
            for (String line : msgLines) {
                byte[] lineBytes = line.getBytes(StandardCharsets.UTF_8);
                armoredOut.write(lineBytes);
                armoredOut.write(crlf);
                signatureGenerator.update(lineBytes);
                signatureGenerator.update(crlf);
            }

            // 7. 结束清文部分，生成签名并写入
            armoredOut.endClearText();
            signatureGenerator.generate().encode(armoredOut);
        }

        byte[] clearSignedData = clearSignedOut.toByteArray();
        String clearSignedText = clearSignedOut.toString(StandardCharsets.UTF_8);

        // 8. 验证签名结果包含预期格式
        assertTrue(clearSignedText.contains("-----BEGIN PGP SIGNED MESSAGE-----"),
                "清文签名应包含 BEGIN PGP SIGNED MESSAGE 标记");
        assertTrue(clearSignedText.contains("Hash: SHA256"),
                "清文签名应包含 Hash 头");
        assertTrue(clearSignedText.contains("-----BEGIN PGP SIGNATURE-----"),
                "清文签名应包含 BEGIN PGP SIGNATURE 标记");
        assertTrue(clearSignedText.contains("-----END PGP SIGNATURE-----"),
                "清文签名应包含 END PGP SIGNATURE 标记");

        // 9. 打印签名结果
        System.out.println("--- 清文签名结果 ---");
        System.out.println(clearSignedText);
        System.out.println("--- 签名结果结束 ---");
        System.out.println("清文签名总长度: " + clearSignedData.length + " 字节");

        // 导出清文签名到临时文件
        Path clearSignedPath = tempDir.resolve("clear-signed.txt");
        Files.write(clearSignedPath, clearSignedData);
        System.out.println("清文签名已导出至: " + clearSignedPath.toAbsolutePath());

        // ========== 验证流程 ==========

        // 10. 从清文签名文本中提取签名部分
        //     清文签名的结构：
        //     -----BEGIN PGP SIGNED MESSAGE-----
        //     Hash: SHA256
        //     <可读文本>
        //     -----BEGIN PGP SIGNATURE-----
        //     <base64 签名数据>
        //     -----END PGP SIGNATURE-----
        String sigBegin = "-----BEGIN PGP SIGNATURE-----";
        String sigEnd = "-----END PGP SIGNATURE-----";
        int sigBeginIdx = clearSignedText.indexOf(sigBegin);
        int sigEndIdx = clearSignedText.indexOf(sigEnd);
        assertTrue(sigBeginIdx >= 0, "应包含签名起始标记");
        assertTrue(sigEndIdx >= 0, "应包含签名结束标记");

        // 提取签名部分（从 BEGIN 到 END，包含标记）
        String sigSection = clearSignedText.substring(sigBeginIdx,
                sigEndIdx + sigEnd.length());
        System.out.println("签名部分长度: " + sigSection.length() + " 字符");

        // 11. 收集清文部分的每一行（用于签名验证）
        //     清文在 "Hash: SHA256" 行之后、"-----BEGIN PGP SIGNATURE-----" 之前
        String[] allLines = clearSignedText.split("\r?\n");
        List<byte[]> clearTextLines = new ArrayList<>();
        boolean inClearText = false;
        for (String line : allLines) {
            if (line.startsWith("-----BEGIN PGP SIGNATURE-----")) {
                break;
            }
            if (line.startsWith("Hash:")) {
                inClearText = true;
                continue;
            }
            if (line.startsWith("-----BEGIN PGP SIGNED MESSAGE-----")) {
                continue;
            }
            if (inClearText && line.isEmpty()) {
                // 空行也需要收集（清文签名要求保留空行）
                continue;
            }
            if (inClearText) {
                clearTextLines.add(line.getBytes(StandardCharsets.UTF_8));
            }
        }
        System.out.println("收集到清文行数: " + clearTextLines.size());

        // 12. 使用 PGPUtil.getDecoderStream 解码签名部分
        PGPSignature pgpSig;
        try (ByteArrayInputStream sigBais = new ByteArrayInputStream(
                sigSection.getBytes(StandardCharsets.UTF_8));
             InputStream decodedIn = PGPUtil.getDecoderStream(sigBais)) {

            // 使用 BCPGInputStream 读取解码后的二进制签名数据
            BCPGInputStream bcpgIn = new BCPGInputStream(decodedIn);
            pgpSig = new PGPSignature(bcpgIn);
        }

        // 13. 使用公钥初始化签名验证器
        pgpSig.init(new JcaPGPContentVerifierBuilderProvider().setProvider("BC"),
                signingPublicKey);

        // 14. 用收集到的清文行更新签名验证
        for (byte[] lineBytes : clearTextLines) {
            pgpSig.update(lineBytes);
            pgpSig.update(crlf);
        }

        // 15. 验证签名
        assertTrue(pgpSig.verify(), "清文签名验证应该通过");

        System.out.println("清文签名测试通过");
    }
}
