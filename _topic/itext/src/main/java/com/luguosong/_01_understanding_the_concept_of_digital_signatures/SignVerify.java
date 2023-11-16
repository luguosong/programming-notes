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
import java.util.Arrays;

/**
 * 签名和验签
 *
 * @author luguosong
 */
public class SignVerify {
    public static void main(String[] args) throws KeyStoreException, IOException, UnrecoverableKeyException, NoSuchAlgorithmException, CertificateException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        //创建并初始化密钥库
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(Files.newInputStream(Paths.get("_topic/itext/src/main/resources/keystore/ks")), "12345678".toCharArray());
        Key publicKey = ks.getCertificate("demo").getPublicKey(); //获取公钥
        Key privateKey = ks.getKey("demo", "12345678".toCharArray()); //获取私钥

        System.out.println("==========签名与验签==========");

        //签名
        String message = "hello pdf";
        System.out.println("原文为：" + message);

        //签名前对原文进行摘要计算
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(message.getBytes());

        Cipher cipher1 = Cipher.getInstance("RSA");
        // 对数据进行签名
        cipher1.init(Cipher.ENCRYPT_MODE, privateKey);
        // 获得加密结果
        byte[] encrypted = cipher1.doFinal(digest);
        // 结果打印
        String signRet = "hello pdf," + new BigInteger(1, encrypted).toString(16);
        System.out.println("将原文与签名结果进行打包：" + signRet);

        // 验签
        Cipher cipher2 = Cipher.getInstance("RSA");
        // 将签名结果解密
        cipher2.init(Cipher.DECRYPT_MODE, publicKey);
        //split[0]为原文，split[1]为签名结果
        String[] split = signRet.split(",");
        byte[] cipherData = cipher2.doFinal(new BigInteger(split[1], 16).toByteArray());
        // 将原文的摘要与验签结果进行比较
        System.out.println("验签结果为：" + Arrays.equals(md.digest(split[0].getBytes()), cipherData));
    }
}
