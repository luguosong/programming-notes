package com.luguosong.chapter01_understanding_the_concept_of_digital_signatures;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * @author luguosong
 */
public class DigestDefault {
    public static void main(String[] args) {
        try {
            /*
             * 也可以是其它摘要算法
             * 比如：SHA-1，SHA-224，SHA-256，SHA-384，SHA-512，RIPEMD128，RIPEMD160，RIPEMD256等
             * */
            byte[] digest; //摘要
            String password = "12345678";
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            digest = messageDigest.digest(password.getBytes(StandardCharsets.UTF_8));

            System.out.println("摘要长度：" + digest.length);
            System.out.println("摘要结果(十六进制)：" + new BigInteger(1, digest).toString(16));
            System.out.println("秘密是不是'secret:'" + Arrays.equals(digest, messageDigest.digest("secret".getBytes(StandardCharsets.UTF_8))));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
