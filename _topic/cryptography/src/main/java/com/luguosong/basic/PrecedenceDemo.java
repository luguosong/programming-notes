package com.luguosong.basic;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;

/**
 * @author luguosong
 */
public class PrecedenceDemo {
    public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchProviderException {
        // 将 BC 添加到优先级列表的末尾
        Security.addProvider(new BouncyCastleProvider());

        System.out.println(MessageDigest.getInstance("SHA1").getProvider().getName()); //SUN
        System.out.println(MessageDigest.getInstance("SHA1", "BC").getProvider().getName()); //BC
    }
}
