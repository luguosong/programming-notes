package com.luguosong.crypto;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.SecretWithEncapsulation;
import org.bouncycastle.pqc.crypto.mlkem.MLKEMExtractor;
import org.bouncycastle.pqc.crypto.mlkem.MLKEMGenerator;
import org.bouncycastle.pqc.crypto.mlkem.MLKEMKeyGenerationParameters;
import org.bouncycastle.pqc.crypto.mlkem.MLKEMKeyPairGenerator;
import org.bouncycastle.pqc.crypto.mlkem.MLKEMParameters;
import org.bouncycastle.pqc.crypto.mlkem.MLKEMPrivateKeyParameters;
import org.bouncycastle.pqc.crypto.mlkem.MLKEMPublicKeyParameters;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ML-KEM（Module-Lattice-Based Key-Encapsulation Mechanism）测试
 * <p>
 * ML-KEM 原名 CRYSTALS-Kyber，是基于格的后量子密钥封装机制，
 * 于 2024 年被 NIST 正式标准化为 FIPS 203。
 * <p>
 * 本测试使用 BouncyCastle 底层 API（非 JCA）演示完整的封装/解封装流程，
 * 可以直接访问共享密钥，更清晰地展示 ML-KEM 的工作原理。
 * <p>
 * 安全等级对比：
 * - ML-KEM-512：NIST 安全等级 1（AES-128 等价）
 * - ML-KEM-768：NIST 安全等级 3（AES-192 等价）
 * - ML-KEM-1024：NIST 安全等级 5（AES-256 等价）
 */
@DisplayName("ML-KEM 密钥封装测试")
class MlKemTest {

    private static final HexFormat HEX = HexFormat.of();
    private static final SecureRandom RANDOM = new SecureRandom();

    @Test
    @DisplayName("ML-KEM-1024 完整封装/解封装流程")
    void testMlKem1024EncapsulateDecapsulate() throws Exception {
        System.out.println("=== ML-KEM-1024 完整流程（BC 底层 API）===");

        // 1. 生成密钥对
        MLKEMKeyPairGenerator kpg = new MLKEMKeyPairGenerator();
        kpg.init(new MLKEMKeyGenerationParameters(RANDOM, MLKEMParameters.ml_kem_1024));
        AsymmetricCipherKeyPair keyPair = kpg.generateKeyPair();

        MLKEMPublicKeyParameters pubKey = (MLKEMPublicKeyParameters) keyPair.getPublic();
        MLKEMPrivateKeyParameters privKey = (MLKEMPrivateKeyParameters) keyPair.getPrivate();

        System.out.println("公钥大小: " + pubKey.getEncoded().length + " 字节");
        System.out.println("私钥大小: " + privKey.getEncoded().length + " 字节");

        // 2. 封装（encapsulate）：用公钥生成密文和共享密钥
        MLKEMGenerator generator = new MLKEMGenerator(RANDOM);
        SecretWithEncapsulation encResult = generator.generateEncapsulated(pubKey);
        byte[] ciphertext = encResult.getEncapsulation();
        byte[] sharedSecretAlice = encResult.getSecret();

        System.out.println("密文大小: " + ciphertext.length + " 字节");
        System.out.println("共享密钥大小: " + sharedSecretAlice.length + " 字节");
        System.out.println("Alice 共享密钥: " + HEX.formatHex(sharedSecretAlice));

        // 3. 解封装（decapsulate）：用私钥从密文恢复共享密钥
        MLKEMExtractor extractor = new MLKEMExtractor(privKey);
        byte[] sharedSecretBob = extractor.extractSecret(ciphertext);

        System.out.println("Bob 共享密钥:   " + HEX.formatHex(sharedSecretBob));

        // 4. 验证双方共享密钥一致
        assertArrayEquals(sharedSecretAlice, sharedSecretBob,
                "封装方和解封装方恢复的共享密钥必须一致");
        System.out.println("验证结果: 共享密钥一致");
    }

    @Test
    @DisplayName("对比 ML-KEM 三种安全等级的密钥/密文大小")
    void testMlKemSecurityLevelsComparison() throws Exception {
        MLKEMParameters[] params = {
                MLKEMParameters.ml_kem_512,
                MLKEMParameters.ml_kem_768,
                MLKEMParameters.ml_kem_1024
        };
        int[] nistLevels = {1, 3, 5};

        System.out.println("=== ML-KEM 三种安全等级对比 ===");
        System.out.printf("%-16s %-6s %-10s %-10s %-10s %-14s%n",
                "算法", "NIST", "公钥(B)", "私钥(B)", "密文(B)", "共享密钥(B)");
        System.out.println("-".repeat(76));

        for (int i = 0; i < params.length; i++) {
            // 生成密钥对
            MLKEMKeyPairGenerator kpg = new MLKEMKeyPairGenerator();
            kpg.init(new MLKEMKeyGenerationParameters(RANDOM, params[i]));
            AsymmetricCipherKeyPair keyPair = kpg.generateKeyPair();

            // 封装获取密文和共享密钥
            MLKEMGenerator generator = new MLKEMGenerator(RANDOM);
            SecretWithEncapsulation encResult = generator.generateEncapsulated(keyPair.getPublic());

            int pubKeyLen = ((MLKEMPublicKeyParameters) keyPair.getPublic()).getEncoded().length;
            int privKeyLen = ((MLKEMPrivateKeyParameters) keyPair.getPrivate()).getEncoded().length;
            int cipherLen = encResult.getEncapsulation().length;
            int secretLen = encResult.getSecret().length;

            System.out.printf("%-16s %-6d %-10d %-10d %-10d %-14d%n",
                    params[i].getName(), nistLevels[i], pubKeyLen, privKeyLen, cipherLen, secretLen);

            assertTrue(pubKeyLen > 0, params[i].getName() + " 公钥大小应大于 0");
            assertTrue(cipherLen > 0, params[i].getName() + " 密文大小应大于 0");
            assertTrue(secretLen > 0, params[i].getName() + " 共享密钥大小应大于 0");
        }

        System.out.println("\n结论：安全等级越高，密钥/密文/共享密钥越大");
    }

    @Test
    @DisplayName("错误的密文应导致解封装产生不同的共享密钥")
    void testDecapsulateWithWrongCiphertext() throws Exception {
        // 生成密钥对
        MLKEMKeyPairGenerator kpg = new MLKEMKeyPairGenerator();
        kpg.init(new MLKEMKeyGenerationParameters(RANDOM, MLKEMParameters.ml_kem_768));
        AsymmetricCipherKeyPair keyPair = kpg.generateKeyPair();
        MLKEMPrivateKeyParameters privKey = (MLKEMPrivateKeyParameters) keyPair.getPrivate();

        // 正确流程生成共享密钥
        MLKEMGenerator generator = new MLKEMGenerator(RANDOM);
        SecretWithEncapsulation correctEnc = generator.generateEncapsulated(keyPair.getPublic());
        byte[] correctSecret = correctEnc.getSecret();

        // 用伪造密文解封装
        MLKEMExtractor extractor = new MLKEMExtractor(privKey);
        byte[] fakeCiphertext = new byte[extractor.getEncapsulationLength()];
        RANDOM.nextBytes(fakeCiphertext);

        // ML-KEM 规范：错误密文不会抛异常，而是返回不同的共享密钥
        byte[] wrongSecret = extractor.extractSecret(fakeCiphertext);

        assertNotEquals(
                HEX.formatHex(correctSecret),
                HEX.formatHex(wrongSecret),
                "错误密文产生的共享密钥应与正确密文不同");
        System.out.println("正确密文共享密钥: " + HEX.formatHex(correctSecret));
        System.out.println("错误密文共享密钥: " + HEX.formatHex(wrongSecret));
        System.out.println("验证结果: 错误密文导致共享密钥不同");
    }
}
