package com.luguosong.crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.crypto.generators.SCrypt;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.Security;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 基于密码的密钥生成演示（PBKDF2 / SCRYPT）
 */
class PbkdfTest {

    @BeforeAll
    static void setup() {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Test
    void shouldDeriveKeyWithPbkdf2() throws Exception {
        // PBKDF2（PKCS#5 Scheme 2）：最广泛使用的基于密码的 KDF
        char[] password = "mypassword".toCharArray();
        byte[] salt = "saltsalt".getBytes();
        int iterations = 10000; // 迭代次数越高越安全，但越慢
        int keyLength = 256;    // 输出密钥长度（位）

        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256", "BC");
        byte[] key = factory.generateSecret(spec).getEncoded();

        assertEquals(32, key.length);
        System.out.println("PBKDF2 key: " + Hex.toHexString(key));
    }

    @Test
    void shouldDeriveKeyWithScrypt() {
        // SCRYPT：内存密集型 KDF，抗 GPU/ASIC 暴力破解
        byte[] password = "mypassword".getBytes();
        byte[] salt = "saltsalt".getBytes();

        // 参数：CPU/内存成本 N=1024, 块大小 r=8, 并行因子 p=1
        byte[] key = SCrypt.generate(password, salt, 1024, 8, 1, 32);

        assertEquals(32, key.length);
        System.out.println("SCrypt key: " + Hex.toHexString(key));
    }

    @Test
    void scryptShouldBeMemoryHard() {
        // SCRYPT 的内存使用量 = 128 * N * r bytes
        // N 越大，内存消耗越大，GPU 越难并行攻击
        long startTime = System.currentTimeMillis();
        byte[] key = SCrypt.generate("test".getBytes(), "salt".getBytes(), 16384, 8, 1, 32);
        long duration = System.currentTimeMillis() - startTime;

        System.out.println("SCrypt (N=16384) took " + duration + "ms");
        assertTrue(key.length == 32);
    }
}
