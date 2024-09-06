package com.luguosong.chapter01_understanding_the_concept_of_digital_signatures;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;

/**
 * @author luguosong
 */
public class EncryptDecrypt {
    public static void main(String[] args) {
        try {
            //初始化密钥库
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(new FileInputStream("docs/topics/itext-signatures/src/main/resources/ks"), "12345678".toCharArray());

            //获取公钥
            Key publicKey = ks.getCertificate("demo").getPublicKey();
            Key privateKey = ks.getKey("demo", "12345678".toCharArray());


            // 使用公钥加密，私钥解密
            String message = "hello iText";
            System.out.println("原文：" + message);
            //加密
            Cipher cipherEncrypt = Cipher.getInstance("RSA");
            cipherEncrypt.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encrypted = cipherEncrypt.doFinal(message.getBytes());
            System.out.println("加密后的结果：" + new BigInteger(1, encrypted).toString(16));
            //解密
            Cipher cipherDecrypt = Cipher.getInstance("RSA");
            cipherDecrypt.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decrypted = cipherDecrypt.doFinal(encrypted);
            System.out.println("解密结果：" + new String(decrypted));


        } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException |
                 UnrecoverableKeyException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException |
                 InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }
}
