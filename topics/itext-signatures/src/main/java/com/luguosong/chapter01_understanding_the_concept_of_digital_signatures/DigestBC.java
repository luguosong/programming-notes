package com.luguosong.chapter01_understanding_the_concept_of_digital_signatures;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Arrays;

/**
 * @author luguosong
 */
public class DigestBC {

    public static final BouncyCastleProvider PROVIDER = new BouncyCastleProvider();

    static {
        Security.addProvider(PROVIDER);
    }

    public static void main(String[] args) {
        try {
            /*
             * 也可以是其它摘要算法
             * 比如：SHA-1，SHA-224，SHA-256，SHA-384，SHA-512，RIPEMD128，RIPEMD160，RIPEMD256等
             * */
            byte[] digest; //摘要
            String password = "12345678";
            MessageDigest messageDigest = MessageDigest.getInstance("SM3", PROVIDER.getName());
            digest = messageDigest.digest(password.getBytes(StandardCharsets.UTF_8));

            System.out.println("摘要长度：" + digest.length);
            System.out.println("摘要结果(十六进制)：" + new BigInteger(1, digest).toString(16));
            System.out.println("秘密是不是'secret:'" + Arrays.equals(digest, messageDigest.digest("secret".getBytes(StandardCharsets.UTF_8))));
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException(e);
        }
    }
}
