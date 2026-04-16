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
 * AES 对称加密基础演示
 */
class AesTest {

    @BeforeAll
    static void setup() {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Test
    void shouldEncryptAndDecryptWithAes() throws Exception {
        // 生成 256 位 AES 密钥
        KeyGenerator keyGen = KeyGenerator.getInstance("AES", "BC");
        keyGen.init(256);
        SecretKey secretKey = keyGen.generateKey();

        // AES/CBC/PKCS7Padding 加密
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC");
        byte[] iv = new byte[16];
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));

        byte[] plainText = "Hello, Cryptography!".getBytes();
        byte[] cipherText = cipher.doFinal(plainText);

        // 解密
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
        byte[] decryptedText = cipher.doFinal(cipherText);

        assertArrayEquals(plainText, decryptedText);
    }

    @Test
    void shouldShowDifferentKeySizes() throws Exception {
        int[] keySizes = {128, 192, 256};
        for (int size : keySizes) {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES", "BC");
            keyGen.init(size);
            SecretKey key = keyGen.generateKey();
            assertEquals(size, key.getEncoded().length * 8);
            System.out.println("AES-" + size + " 密钥长度: " + key.getEncoded().length + " bytes");
        }
    }
}
