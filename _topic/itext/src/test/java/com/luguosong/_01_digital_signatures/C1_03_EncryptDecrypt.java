package com.luguosong._01_digital_signatures;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;
import java.math.BigInteger;
import java.security.*;

/**
 * @author luguosong
 */
public class C1_03_EncryptDecrypt {

    /**
     * 公钥
     */
    private PublicKey publicKey;

    /**
     * 私钥
     */
    private Key privateKey;

    /**
     * 密码器
     */
    private Cipher cipher;

    /**
     * 签名器
     */
    private Signature signature;

    /**
     * 初始化方法
     * 从密钥库中加载公钥和私钥，初始化密码器和签名器。
     */
    @BeforeEach
    public void init() {
        try {
            // 从密钥库中加载公钥和私钥
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(C1_03_EncryptDecrypt.class.getClassLoader().getResourceAsStream("01_digital_signatures/ks"), "12345678".toCharArray());
            publicKey = ks.getCertificate("demo").getPublicKey();
            privateKey = ks.getKey("demo", "12345678".toCharArray());
            // 初始化密码器
            cipher = Cipher.getInstance("RSA");
            // 初始化签名器
            signature = Signature.getInstance("SHA256withRSA");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * 加密和解密方法
     * 本方法演示了非对称加解密的过程，包括加密和解密。
     * 加密使用公钥，解密使用私钥。
     */
    @Test
    public void encryptionAndDecryption() {
        try {
            //加密
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encrypt = cipher.doFinal("hello world".getBytes());
            System.out.println("加密结果：" + new BigInteger(1, encrypt).toString(16));

            //解密
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decrypt = cipher.doFinal(encrypt);
            System.out.println("解密结果：" + new String(decrypt));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * 签名和验签方法
     * 本方法演示了签名和验签的过程，包括签名和验签。
     * 签名使用私钥，验签使用公钥。
     * 签名的过程是先对数据进行哈希，然后使用私钥对哈希值进行加密，得到签名。
     * 验签的过程是先对数据进行哈希，然后使用公钥对签名进行解密，得到哈希值，然后比较两个哈希值是否相等。
     */
    @Test
    public void signingAndVerifyingSignatures() {
        try {
            String data = "hello world";

            //签名
            signature.initSign((PrivateKey) privateKey);
            signature.update(data.getBytes());
            byte[] sign = signature.sign();
            System.out.println("签名结果：" + new BigInteger(1, sign).toString(16));

            //验签
            signature.initVerify(publicKey);
            signature.update(data.getBytes());
            boolean verify = signature.verify(sign);
            System.out.println("验签结果：" + verify);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
