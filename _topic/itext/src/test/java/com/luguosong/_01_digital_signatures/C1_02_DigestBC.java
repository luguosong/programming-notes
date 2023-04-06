package com.luguosong._01_digital_signatures;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Arrays;

/**
 * 使用BC库进行摘要计算
 *
 * @author luguosong
 */
public class C1_02_DigestBC {

    /**
     * 方法名：showTest
     * 描述：展示测试
     *
     * @param algorithm 算法
     * @throws RuntimeException 运行时异常
     */
    public void showTest(String algorithm) {
        try {
            // 获取信息摘要实例,使用BC库作为密码提供者
            MessageDigest messageDigest = MessageDigest.getInstance(algorithm, new BouncyCastleProvider());
            // 对字符串进行摘要
            byte[] digest = messageDigest.digest("hello world".getBytes("UTF-8"));
            // 输出摘要结果
            System.out.println("摘要使用 " + algorithm + ": " + digest.length);
            System.out.println("摘要: " + new BigInteger(1, digest).toString(16));
            // 验证摘要结果
            System.out.println("摘要验证 " + Arrays.equals(digest, messageDigest.digest("hello world".getBytes())));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * 方法名：testDigest
     * 描述：测试信息摘要
     */
    @Test
    public void testDigest() {
        showTest("MD5");
        showTest("SHA-1");
        showTest("SHA-224");
        showTest("SHA-256");
        showTest("SHA-384");
        showTest("SHA-512");
        showTest("RIPEMD128");
        showTest("RIPEMD160");
        showTest("RIPEMD256");
    }

}
