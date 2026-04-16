package com.luguosong.crypto;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.SecretWithEncapsulation;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pqc.crypto.mlkem.MLKEMExtractor;
import org.bouncycastle.pqc.crypto.mlkem.MLKEMGenerator;
import org.bouncycastle.pqc.crypto.mlkem.MLKEMKeyGenerationParameters;
import org.bouncycastle.pqc.crypto.mlkem.MLKEMKeyPairGenerator;
import org.bouncycastle.pqc.crypto.mlkem.MLKEMParameters;
import org.bouncycastle.pqc.crypto.mlkem.MLKEMPrivateKeyParameters;
import org.bouncycastle.pqc.crypto.mlkem.MLKEMPublicKeyParameters;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.KeyAgreement;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.Security;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 混合 KEM（Hybrid Key Encapsulation Mechanism）测试
 * <p>
 * 实际生产环境中，推荐同时使用传统 KEM（如 ECDH）和后量子 KEM（如 ML-KEM），
 * 形成混合方案。这样即使量子计算机攻破了 ML-KEM，ECDH 仍然提供安全保障。
 * <p>
 * 混合策略（NIST 推荐）：
 * 1. 分别生成 ECDH 和 ML-KEM 密钥对
 * 2. 分别封装/解封装，得到两份共享密钥
 * 3. 通过 KDF（如 SHA-256/SHA-384）合并两份共享密钥
 * 4. 最终的混合密钥用于后续加密通信
 */
@DisplayName("混合 KEM 测试（ECDH + ML-KEM）")
class HybridKemTest {

    private static final HexFormat HEX = HexFormat.of();
    private static final SecureRandom RANDOM = new SecureRandom();

    @BeforeAll
    static void setUp() {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * 使用 SHA-256 合并两个共享密钥（简单 KDF）
     * 生产环境应使用 HKDF 等标准 KDF
     */
    private static byte[] combineSharedSecrets(byte[] secret1, byte[] secret2) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(secret1);
        digest.update(secret2);
        return digest.digest();
    }

    @Test
    @DisplayName("ECDH + ML-KEM-768 混合密钥交换完整流程")
    void testHybridKemFullFlow() throws Exception {
        System.out.println("=== 混合 KEM 完整流程 ===\n");

        // ===== 第一部分：ECDH（传统密钥交换）=====
        System.out.println("--- ECDH（传统）---");
        KeyPairGenerator ecdhKpg = KeyPairGenerator.getInstance("EC");
        ecdhKpg.initialize(256); // 使用 P-256 曲线
        KeyPair ecdhKeyPair = ecdhKpg.generateKeyPair();

        System.out.println("ECDH 公钥大小: " + ecdhKeyPair.getPublic().getEncoded().length + " 字节");

        // ECDH 通过 KeyAgreement 实现
        KeyPairGenerator ecdhPeerKpg = KeyPairGenerator.getInstance("EC");
        ecdhPeerKpg.initialize(256);
        KeyPair ecdhPeerPair = ecdhPeerKpg.generateKeyPair();

        KeyAgreement ecdhKa = KeyAgreement.getInstance("ECDH");
        ecdhKa.init(ecdhKeyPair.getPrivate());
        ecdhKa.doPhase(ecdhPeerPair.getPublic(), true);
        byte[] ecdhSecretAlice = ecdhKa.generateSecret();

        KeyAgreement ecdhKaBob = KeyAgreement.getInstance("ECDH");
        ecdhKaBob.init(ecdhPeerPair.getPrivate());
        ecdhKaBob.doPhase(ecdhKeyPair.getPublic(), true);
        byte[] ecdhSecretBob = ecdhKaBob.generateSecret();

        System.out.println("ECDH 共享密钥: " + HEX.formatHex(ecdhSecretAlice));
        assertArrayEquals(ecdhSecretAlice, ecdhSecretBob, "ECDH 双方共享密钥必须一致");

        // ===== 第二部分：ML-KEM（后量子密钥封装）=====
        System.out.println("\n--- ML-KEM-768（后量子）---");
        MLKEMKeyPairGenerator mlKemKpg = new MLKEMKeyPairGenerator();
        mlKemKpg.init(new MLKEMKeyGenerationParameters(RANDOM, MLKEMParameters.ml_kem_768));
        AsymmetricCipherKeyPair mlKemKeyPair = mlKemKpg.generateKeyPair();
        MLKEMPublicKeyParameters mlKemPubKey = (MLKEMPublicKeyParameters) mlKemKeyPair.getPublic();
        MLKEMPrivateKeyParameters mlKemPrivKey = (MLKEMPrivateKeyParameters) mlKemKeyPair.getPrivate();

        System.out.println("ML-KEM 公钥大小: " + mlKemPubKey.getEncoded().length + " 字节");

        // 封装：用公钥生成密文和共享密钥
        MLKEMGenerator mlKemGen = new MLKEMGenerator(RANDOM);
        SecretWithEncapsulation mlKemEncResult = mlKemGen.generateEncapsulated(mlKemPubKey);
        byte[] mlKemCiphertext = mlKemEncResult.getEncapsulation();
        byte[] mlKemSecretAlice = mlKemEncResult.getSecret();

        // 解封装：用私钥恢复共享密钥
        MLKEMExtractor mlKemExt = new MLKEMExtractor(mlKemPrivKey);
        byte[] mlKemSecretBob = mlKemExt.extractSecret(mlKemCiphertext);

        System.out.println("ML-KEM 密文大小: " + mlKemCiphertext.length + " 字节");
        System.out.println("ML-KEM 共享密钥: " + HEX.formatHex(mlKemSecretAlice));
        assertArrayEquals(mlKemSecretAlice, mlKemSecretBob, "ML-KEM 双方共享密钥必须一致");

        // ===== 第三部分：KDF 合并两份共享密钥 =====
        System.out.println("\n--- KDF 合并共享密钥 ---");
        byte[] combinedAlice = combineSharedSecrets(ecdhSecretAlice, mlKemSecretAlice);
        byte[] combinedBob = combineSharedSecrets(ecdhSecretBob, mlKemSecretBob);

        System.out.println("Alice 混合密钥: " + HEX.formatHex(combinedAlice));
        System.out.println("Bob 混合密钥:   " + HEX.formatHex(combinedBob));

        assertArrayEquals(combinedAlice, combinedBob,
                "混合后的共享密钥双方必须一致");
        System.out.println("\n验证结果: 混合密钥交换成功");
        System.out.println("混合密钥大小: " + combinedAlice.length + " 字节（SHA-256 输出）");
    }

    @Test
    @DisplayName("混合方案安全性分析：单点突破不影响整体安全")
    void testHybridSecurityAnalysis() throws Exception {
        System.out.println("=== 混合方案安全性分析 ===\n");

        // ECDH 部分
        KeyPairGenerator ecdhKpg = KeyPairGenerator.getInstance("EC");
        ecdhKpg.initialize(256);
        KeyPair ecdhAlice = ecdhKpg.generateKeyPair();
        KeyPair ecdhBob = ecdhKpg.generateKeyPair();

        KeyAgreement ka = KeyAgreement.getInstance("ECDH");
        ka.init(ecdhAlice.getPrivate());
        ka.doPhase(ecdhBob.getPublic(), true);
        byte[] realEcdhSecret = ka.generateSecret();

        // ML-KEM 部分
        MLKEMKeyPairGenerator mlKemKpg = new MLKEMKeyPairGenerator();
        mlKemKpg.init(new MLKEMKeyGenerationParameters(RANDOM, MLKEMParameters.ml_kem_768));
        AsymmetricCipherKeyPair mlKemKeyPair = mlKemKpg.generateKeyPair();

        MLKEMGenerator mlKemGen = new MLKEMGenerator(RANDOM);
        SecretWithEncapsulation mlKemEncResult = mlKemGen.generateEncapsulated(mlKemKeyPair.getPublic());
        byte[] realMlKemSecret = mlKemEncResult.getSecret();

        byte[] correctCombined = combineSharedSecrets(realEcdhSecret, realMlKemSecret);

        // 模拟场景 1：量子攻击者破解了 ML-KEM，但不知道 ECDH 密钥
        byte[] fakeMlKemSecret = new byte[realMlKemSecret.length];
        RANDOM.nextBytes(fakeMlKemSecret);
        byte[] quantumAttackerCombined = combineSharedSecrets(realEcdhSecret, fakeMlKemSecret);

        // 模拟场景 2：经典攻击者破解了 ECDH，但不知道 ML-KEM 密钥
        byte[] fakeEcdhSecret = new byte[realEcdhSecret.length];
        RANDOM.nextBytes(fakeEcdhSecret);
        byte[] classicAttackerCombined = combineSharedSecrets(fakeEcdhSecret, realMlKemSecret);

        System.out.println("正确混合密钥:   " + HEX.formatHex(correctCombined));
        System.out.println("量子攻击者密钥:  " + HEX.formatHex(quantumAttackerCombined));
        System.out.println("经典攻击者密钥:  " + HEX.formatHex(classicAttackerCombined));

        assertNotEquals(HEX.formatHex(correctCombined), HEX.formatHex(quantumAttackerCombined),
                "量子攻击者即使破解 ML-KEM，也因不知 ECDH 密钥而无法得到混合密钥");
        assertNotEquals(HEX.formatHex(correctCombined), HEX.formatHex(classicAttackerCombined),
                "经典攻击者即使破解 ECDH，也因不知 ML-KEM 密钥而无法得到混合密钥");

        System.out.println("\n结论：混合方案中任一算法被破解，攻击者仍无法得到最终密钥");
    }

    @Test
    @DisplayName("三种 ML-KEM 安全等级在混合方案中的开销对比")
    void testHybridOverheadComparison() throws Exception {
        System.out.println("=== 混合方案开销对比（ECDH P-256 + ML-KEM）===\n");

        // ECDH P-256 基准
        KeyPairGenerator ecdhKpg = KeyPairGenerator.getInstance("EC");
        ecdhKpg.initialize(256);
        KeyPair ecdhKeyPair = ecdhKpg.generateKeyPair();
        int ecdhPubKeyLen = ecdhKeyPair.getPublic().getEncoded().length;

        System.out.printf("%-16s %-12s %-14s %-14s%n",
                "算法", "公钥总大小", "密文总大小", "混合密钥大小");
        System.out.println("-".repeat(60));

        MLKEMParameters[] mlKemParams = {
                MLKEMParameters.ml_kem_512,
                MLKEMParameters.ml_kem_768,
                MLKEMParameters.ml_kem_1024
        };

        for (MLKEMParameters param : mlKemParams) {
            MLKEMKeyPairGenerator kpg = new MLKEMKeyPairGenerator();
            kpg.init(new MLKEMKeyGenerationParameters(RANDOM, param));
            AsymmetricCipherKeyPair keyPair = kpg.generateKeyPair();

            MLKEMGenerator generator = new MLKEMGenerator(RANDOM);
            SecretWithEncapsulation encResult = generator.generateEncapsulated(keyPair.getPublic());

            int mlKemPubKeyLen = ((MLKEMPublicKeyParameters) keyPair.getPublic()).getEncoded().length;
            int mlKemCipherLen = encResult.getEncapsulation().length;

            int totalPubKey = ecdhPubKeyLen + mlKemPubKeyLen;
            int totalCipher = ecdhPubKeyLen + mlKemCipherLen; // ECDH 密文约等于对方公钥大小
            int combinedKey = 32; // SHA-256 输出固定 32 字节

            System.out.printf("%-16s %-12d %-14d %-14d%n",
                    param.getName(), totalPubKey, totalCipher, combinedKey);
        }

        System.out.println("\n说明：混合方案额外增加的主要是公钥传输开销");
    }
}
