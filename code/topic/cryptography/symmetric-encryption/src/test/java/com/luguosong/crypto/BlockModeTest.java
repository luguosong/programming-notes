package com.luguosong.crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.Security;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 分组密码工作模式对比演示
 */
class BlockModeTest {

    private static SecretKey aesKey;

    @BeforeAll
    static void setup() throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        KeyGenerator keyGen = KeyGenerator.getInstance("AES", "BC");
        keyGen.init(256);
        aesKey = keyGen.generateKey();
    }

    @Test
    void shouldWorkWithECB() throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding", "BC");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);

        byte[] plainText = "0123456789ABCDEF".getBytes();
        byte[] cipherText = cipher.doFinal(plainText);

        cipher.init(Cipher.DECRYPT_MODE, aesKey);
        byte[] decrypted = cipher.doFinal(cipherText);

        assertArrayEquals(plainText, decrypted);
    }

    @Test
    void shouldWorkWithCBC() throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC");
        byte[] iv = new byte[16];

        cipher.init(Cipher.ENCRYPT_MODE, aesKey, new IvParameterSpec(iv));
        byte[] cipherText = cipher.doFinal("CBC mode demo".getBytes());

        cipher.init(Cipher.DECRYPT_MODE, aesKey, new IvParameterSpec(iv));
        byte[] decrypted = cipher.doFinal(cipherText);

        assertEquals("CBC mode demo", new String(decrypted));
    }

    @Test
    void shouldWorkWithCTR() throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding", "BC");
        byte[] iv = new byte[16];

        cipher.init(Cipher.ENCRYPT_MODE, aesKey, new IvParameterSpec(iv));
        byte[] cipherText = cipher.doFinal("CTR mode turns block into stream".getBytes());

        cipher.init(Cipher.DECRYPT_MODE, aesKey, new IvParameterSpec(iv));
        byte[] decrypted = cipher.doFinal(cipherText);

        assertEquals("CTR mode turns block into stream", new String(decrypted));
    }
}
