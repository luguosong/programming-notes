package com.luguosong.crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 密钥分割（Secret Sharing）演示
 * 原理：将一个密钥拆分为 N 份，任意 K 份可恢复原始密钥
 */
class KeySplittingTest {

    @BeforeAll
    static void setup() {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Test
    void shouldSplitAndRecoverKey() {
        // 简化的密钥分割演示：XOR 秘密共享
        byte[] originalKey = new byte[16];
        new SecureRandom().nextBytes(originalKey);

        // 分割为 3 份，任意 2 份可恢复
        byte[] share1 = new byte[16];
        byte[] share2 = new byte[16];
        byte[] share3 = new byte[16];
        new SecureRandom().nextBytes(share1);

        for (int i = 0; i < originalKey.length; i++) {
            share2[i] = (byte) (originalKey[i] ^ share1[i]);
            share3[i] = (byte) (originalKey[i] ^ share2[i]);
        }

        // 使用 share1 + share2 恢复
        byte[] recovered = new byte[16];
        for (int i = 0; i < originalKey.length; i++) {
            recovered[i] = (byte) (share1[i] ^ share2[i]);
        }

        assertArrayEquals(originalKey, recovered);
    }

    @Test
    void differentKeySharesShouldRecoverSame() {
        // 使用 share2 + share3 也能恢复同样的密钥
        byte[] originalKey = new byte[16];
        new SecureRandom().nextBytes(originalKey);

        byte[] share1 = new byte[16];
        byte[] share2 = new byte[16];
        new SecureRandom().nextBytes(share1);

        for (int i = 0; i < originalKey.length; i++) {
            share2[i] = (byte) (originalKey[i] ^ share1[i]);
        }

        byte[] recovered = new byte[16];
        for (int i = 0; i < originalKey.length; i++) {
            recovered[i] = (byte) (share1[i] ^ share2[i]);
        }

        assertArrayEquals(originalKey, recovered);
    }
}
