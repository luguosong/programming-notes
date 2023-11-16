package com.luguosong._01_understanding_the_concept_of_digital_signatures;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;

/**
 * 加密与解密
 * @author luguosong
 */
public class EncryptDecrypt {
    public static void main(String[] args) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        //创建并初始化密钥库
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(Files.newInputStream(Paths.get("_topic/itext/src/main/resources/keystore/ks")), "12345678".toCharArray());
        Key publicKey = ks.getCertificate("demo").getPublicKey(); //获取公钥
        Key privateKey = ks.getKey("demo", "12345678".toCharArray()); //获取私钥

        System.out.println("==========加密与解密==========");

        //加密
        String message = "hello pdf";
        System.out.println("原文为：" + message);
        Cipher cipher1 = Cipher.getInstance("RSA");
        //加密模式,使用提供的公钥进行数据加密。
        cipher1.init(Cipher.ENCRYPT_MODE, publicKey);
        // 获得加密结果
        byte[] encrypted = cipher1.doFinal(message.getBytes());
        // 结果打印
        System.out.println("加密后的内容为：" + new BigInteger(1, encrypted).toString(16));

        // 解密
        Cipher cipher2 = Cipher.getInstance("RSA");
        // 解密模式,使用提供的私钥进行数据解密。
        cipher2.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] cipherData = cipher2.doFinal(encrypted);
        // 结果打印
        System.out.println("解密后的内容为：" + new String(cipherData));
    }
}
