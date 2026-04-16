package com.luguosong.crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.crypto.KeyAgreement;
import java.security.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ECDH（椭圆曲线 Diffie-Hellman）密钥协商演示
 */
class EcdhTest {

    @BeforeAll
    static void setup() {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Test
    void shouldAgreeOnSharedSecretViaECDH() throws Exception {
        // ECDH：基于椭圆曲线的 DH，密钥更短但安全性等价
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("ECDH", "BC");
        kpg.initialize(256); // 256 位椭圆曲线 ≈ 3072 位 DH 的安全性

        // Alice
        KeyPair aliceKeyPair = kpg.generateKeyPair();
        KeyAgreement aliceKa = KeyAgreement.getInstance("ECDH", "BC");
        aliceKa.init(aliceKeyPair.getPrivate());

        // Bob
        KeyPair bobKeyPair = kpg.generateKeyPair();
        KeyAgreement bobKa = KeyAgreement.getInstance("ECDH", "BC");
        bobKa.init(bobKeyPair.getPrivate());

        // 交换公钥并计算共享密钥
        aliceKa.doPhase(bobKeyPair.getPublic(), true);
        bobKa.doPhase(aliceKeyPair.getPublic(), true);

        byte[] aliceSecret = aliceKa.generateSecret();
        byte[] bobSecret = bobKa.generateSecret();

        assertArrayEquals(aliceSecret, bobSecret);
        System.out.println("ECDH shared secret: " + aliceSecret.length + " bytes");
    }

    @Test
    void differentKeyPairsShouldProduceDifferentSecrets() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("ECDH", "BC");
        kpg.initialize(256);

        // 第一对
        KeyPair kp1a = kpg.generateKeyPair();
        KeyPair kp1b = kpg.generateKeyPair();
        KeyAgreement ka1 = KeyAgreement.getInstance("ECDH", "BC");
        ka1.init(kp1a.getPrivate());
        ka1.doPhase(kp1b.getPublic(), true);
        byte[] secret1 = ka1.generateSecret();

        // 第二对
        KeyPair kp2a = kpg.generateKeyPair();
        KeyPair kp2b = kpg.generateKeyPair();
        KeyAgreement ka2 = KeyAgreement.getInstance("ECDH", "BC");
        ka2.init(kp2a.getPrivate());
        ka2.doPhase(kp2b.getPublic(), true);
        byte[] secret2 = ka2.generateSecret();

        assertFalse(MessageDigest.isEqual(secret1, secret2));
    }
}
