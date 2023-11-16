package com.luguosong._01_understanding_the_concept_of_digital_signatures;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 计算摘要
 *
 * @author luguosong
 */
public class DigestDefault {
    public static void main(String[] args) throws NoSuchAlgorithmException {
        String message = "12345678";
        System.out.println("原文：" + message);
        // 通过引擎类获取MD5算法实例
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        //计算摘要
        byte[] digest = messageDigest.digest(message.getBytes());
        //打印结果
        System.out.println("十六进制结果：" + new BigInteger(1, digest).toString(16));
        //结果长度
        System.out.println("结果长度：" + digest.length);
    }
}
