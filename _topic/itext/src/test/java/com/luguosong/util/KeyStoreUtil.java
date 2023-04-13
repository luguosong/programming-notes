package com.luguosong.util;

import com.luguosong._01_digital_signatures.C1_03_EncryptDecrypt;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;

/**
 * @author luguosong
 */
public class KeyStoreUtil {
    private static KeyStore ks;

    private static String alias;

    static {
        try {
            // 获取密钥库
            ks = KeyStore.getInstance(KeyStore.getDefaultType());
            // 读取密钥库文件
            ks.load(C1_03_EncryptDecrypt.class.getClassLoader().getResourceAsStream("01_digital_signatures/ks"), "12345678".toCharArray());
            alias = ks.aliases().nextElement();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * 获取私钥
     *
     * @return
     */
    public static PrivateKey getPrivateKey() {
        try {
            return (PrivateKey) ks.getKey(alias, "12345678".toCharArray());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取证书链
     *
     * @return
     */
    public static Certificate[] getCertificates() {
        try {
            return ks.getCertificateChain(alias);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
