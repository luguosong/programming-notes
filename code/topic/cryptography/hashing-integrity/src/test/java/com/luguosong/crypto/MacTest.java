package com.luguosong.crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.Strings;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.Security;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MAC（消息认证码）与 HMAC 演示
 */
class MacTest {

    @BeforeAll
    static void setup() {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Test
    void shouldComputeHmacSha256() throws Exception {
        javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256", "BC");
        SecretKey key = new SecretKeySpec(
                Hex.decode("2ccd85dfc8d18cb5d84fef4b198554699fece6e8692c9147b0da983f5b7bd413"),
                "HmacSHA256"
        );
        mac.init(key);
        byte[] macValue = mac.doFinal(Strings.toByteArray("Hello World!"));

        assertEquals(32, macValue.length); // HMAC-SHA256 = 32 bytes
        System.out.println("HMAC-SHA256: " + Hex.toHexString(macValue));
    }

    @Test
    void shouldComputeAesCmac() throws Exception {
        // AES-CMAC：基于 AES 的消息认证码
        javax.crypto.Mac mac = javax.crypto.Mac.getInstance("AESCMAC", "BC");
        SecretKey key = new SecretKeySpec(
                Hex.decode("000102030405060708090a0b0c0d0e0f1011121314151617"),
                "AES"
        );
        mac.init(key);
        byte[] macValue = mac.doFinal(Strings.toByteArray("Hello World!"));

        assertEquals(16, macValue.length); // AES-CMAC = 16 bytes（一个 AES 块）
        System.out.println("AES-CMAC: " + Hex.toHexString(macValue));
    }
}
