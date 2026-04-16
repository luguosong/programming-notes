package com.luguosong.crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.security.MessageDigest;
import java.security.Security;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 消息摘要演示（SHA-256 / SHA-3）
 */
class HashTest {

    @BeforeAll
    static void setup() {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Test
    void shouldComputeSha256() throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256", "BC");
        byte[] hash = digest.digest("Hello World!".getBytes());

        // SHA-256 输出 32 字节
        assertEquals(32, hash.length);
        System.out.println("SHA-256: " + Hex.toHexString(hash));
    }

    @Test
    void shouldComputeSha3() throws Exception {
        // SHA-3 是 NIST 2015 年发布的新一代哈希算法（Keccak）
        MessageDigest digest = MessageDigest.getInstance("SHA3-256", "BC");
        byte[] hash = digest.digest("Hello World!".getBytes());

        assertEquals(32, hash.length);
        System.out.println("SHA3-256: " + Hex.toHexString(hash));
    }

    @Test
    void shouldVerifyDigestIntegrity() throws Exception {
        // 消息摘要用于验证数据完整性：相同输入 → 相同输出
        MessageDigest digest = MessageDigest.getInstance("SHA-256", "BC");

        byte[] hash1 = digest.digest("test data".getBytes());
        byte[] hash2 = digest.digest("test data".getBytes());
        byte[] hash3 = digest.digest("test datf".getBytes()); // 篡改一个字节

        assertArrayEquals(hash1, hash2); // 相同数据 → 相同摘要
        assertFalse(MessageDigest.isEqual(hash1, hash3)); // 篡改数据 → 不同摘要
    }
}
