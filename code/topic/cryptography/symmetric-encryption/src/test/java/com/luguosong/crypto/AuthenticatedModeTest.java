package com.luguosong.crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import java.security.Security;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 认证加密模式演示（GCM / EAX）
 */
class AuthenticatedModeTest {

    private static SecretKey aesKey;

    @BeforeAll
    static void setup() throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        KeyGenerator keyGen = KeyGenerator.getInstance("AES", "BC");
        keyGen.init(256);
        aesKey = keyGen.generateKey();
    }

    @Test
    void shouldEncryptAndAuthenticateWithGCM() throws Exception {
        byte[] iv = new byte[12]; // GCM 推荐 12 字节 IV
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding", "BC");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, new GCMParameterSpec(128, iv));
        byte[] cipherText = cipher.doFinal("Authenticated encryption demo".getBytes());

        cipher.init(Cipher.DECRYPT_MODE, aesKey, new GCMParameterSpec(128, iv));
        byte[] decrypted = cipher.doFinal(cipherText);

        assertEquals("Authenticated encryption demo", new String(decrypted));
    }

    @Test
    void shouldDetectTamperingWithGCM() throws Exception {
        byte[] iv = new byte[12];
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding", "BC");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, new GCMParameterSpec(128, iv));
        byte[] cipherText = cipher.doFinal("secret data".getBytes());

        // 篡改密文
        byte[] tampered = Arrays.copyOf(cipherText, cipherText.length);
        tampered[0] ^= 0x01;

        cipher.init(Cipher.DECRYPT_MODE, aesKey, new GCMParameterSpec(128, iv));
        assertThrows(AEADBadTagException.class, () -> cipher.doFinal(tampered));
    }

    @Test
    void shouldWorkWithEAX() throws Exception {
        byte[] iv = new byte[16];
        Cipher cipher = Cipher.getInstance("AES/EAX/NoPadding", "BC");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, new IvParameterSpec(iv));
        byte[] cipherText = cipher.doFinal("EAX mode in Bouncy Castle".getBytes());

        cipher.init(Cipher.DECRYPT_MODE, aesKey, new IvParameterSpec(iv));
        byte[] decrypted = cipher.doFinal(cipherText);

        assertEquals("EAX mode in Bouncy Castle", new String(decrypted));
    }
}
