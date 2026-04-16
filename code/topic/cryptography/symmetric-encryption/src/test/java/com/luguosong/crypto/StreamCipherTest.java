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
 * 流密码演示（ChaCha20-Poly1305）
 */
class StreamCipherTest {

    private static SecretKey chachaKey;

    @BeforeAll
    static void setup() throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        KeyGenerator keyGen = KeyGenerator.getInstance("ChaCha20-Poly1305", "BC");
        keyGen.init(256);
        chachaKey = keyGen.generateKey();
    }

    @Test
    void shouldEncryptWithChaCha20Poly1305() throws Exception {
        byte[] nonce = new byte[12];
        Cipher cipher = Cipher.getInstance("ChaCha20-Poly1305", "BC");
        cipher.init(Cipher.ENCRYPT_MODE, chachaKey, new IvParameterSpec(nonce));

        byte[] plainText = "ChaCha20 stream cipher demo".getBytes();
        byte[] cipherText = cipher.doFinal(plainText);

        cipher.init(Cipher.DECRYPT_MODE, chachaKey, new IvParameterSpec(nonce));
        byte[] decrypted = cipher.doFinal(cipherText);

        assertEquals("ChaCha20 stream cipher demo", new String(decrypted));
    }
}
