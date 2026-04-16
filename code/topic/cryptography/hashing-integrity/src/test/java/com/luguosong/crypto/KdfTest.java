package com.luguosong.crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.security.Security;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 密钥派生函数（KDF）演示
 */
class KdfTest {

    @BeforeAll
    static void setup() {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Test
    void shouldDeriveKeyWithPBKDF2() throws Exception {
        // PBKDF2：基于密码和盐值派生密钥
        char[] password = "my secret password".toCharArray();
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);

        PBEKeySpec spec = new PBEKeySpec(password, salt, 10000, 256);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256", "BC");
        byte[] derivedKey = factory.generateSecret(spec).getEncoded();

        assertEquals(32, derivedKey.length); // 256 位 = 32 bytes
        System.out.println("PBKDF2 derived key: " + Hex.toHexString(derivedKey));
    }

    @Test
    void sameInputShouldProduceSameKey() throws Exception {
        // 相同输入 → 相同输出（确定性派生）
        char[] password = "test".toCharArray();
        byte[] salt = "fixed salt value".getBytes();

        PBEKeySpec spec = new PBEKeySpec(password, salt, 1000, 128);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256", "BC");

        byte[] key1 = factory.generateSecret(spec).getEncoded();
        byte[] key2 = factory.generateSecret(spec).getEncoded();

        assertArrayEquals(key1, key2);
    }

    @Test
    void differentSaltShouldProduceDifferentKey() throws Exception {
        char[] password = "test".toCharArray();

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256", "BC");

        byte[] key1 = factory.generateSecret(new PBEKeySpec(password, "salt1".getBytes(), 1000, 128)).getEncoded();
        byte[] key2 = factory.generateSecret(new PBEKeySpec(password, "salt2".getBytes(), 1000, 128)).getEncoded();

        assertFalse(java.security.MessageDigest.isEqual(key1, key2));
    }
}
