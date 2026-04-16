package com.luguosong.crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.crypto.KeyAgreement;
import javax.crypto.spec.DHParameterSpec;
import java.security.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Diffie-Hellman 密钥协商演示
 */
class KeyAgreementTest {

    @BeforeAll
    static void setup() {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Test
    void shouldAgreeOnSharedSecretViaDH() throws Exception {
        // DH 密钥协商：双方各自生成公私钥对，交换公钥后独立计算出相同的共享密钥
        AlgorithmParameterGenerator paramGen = AlgorithmParameterGenerator.getInstance("DH", "BC");
        paramGen.init(2048);
        AlgorithmParameters params = paramGen.generateParameters();
        DHParameterSpec dhSpec = params.getParameterSpec(DHParameterSpec.class);

        // Alice 生成密钥对
        KeyPairGenerator aliceKpg = KeyPairGenerator.getInstance("DH", "BC");
        aliceKpg.initialize(dhSpec);
        KeyPair aliceKeyPair = aliceKpg.generateKeyPair();

        // Bob 生成密钥对（使用相同参数）
        KeyPairGenerator bobKpg = KeyPairGenerator.getInstance("DH", "BC");
        bobKpg.initialize(dhSpec);
        KeyPair bobKeyPair = bobKpg.generateKeyPair();

        // Alice 计算共享密钥
        KeyAgreement aliceKa = KeyAgreement.getInstance("DH", "BC");
        aliceKa.init(aliceKeyPair.getPrivate());
        aliceKa.doPhase(bobKeyPair.getPublic(), true);
        byte[] aliceSharedSecret = aliceKa.generateSecret();

        // Bob 计算共享密钥
        KeyAgreement bobKa = KeyAgreement.getInstance("DH", "BC");
        bobKa.init(bobKeyPair.getPrivate());
        bobKa.doPhase(aliceKeyPair.getPublic(), true);
        byte[] bobSharedSecret = bobKa.generateSecret();

        // 双方独立计算出相同的共享密钥
        assertArrayEquals(aliceSharedSecret, bobSharedSecret);
        assertTrue(aliceSharedSecret.length > 0);
        System.out.println("DH shared secret length: " + aliceSharedSecret.length + " bytes");
    }
}
