package com.luguosong.crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.Test;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 演示 JCA Provider 架构：优先级、注册、能力查询
 */
class ProviderArchitectureTest {

    @Test
    void bcProviderShouldBeLastInPrecedence() throws NoSuchAlgorithmException, NoSuchProviderException {
        // 添加 BC 到优先级列表末尾
        Security.addProvider(new BouncyCastleProvider());

        // 不指定 Provider 时，使用优先级最高的实现（JDK 内置 SUN）
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        assertEquals("SUN", digest.getProvider().getName());

        // 指定 Provider 名称
        MessageDigest bcDigest = MessageDigest.getInstance("SHA-1", "BC");
        assertEquals("BC", bcDigest.getProvider().getName());
    }

    @Test
    void shouldListInstalledProviders() {
        Provider[] providers = Security.getProviders();
        assertTrue(providers.length > 0);

        // 打印所有 Provider 名称（供学习参考）
        for (Provider provider : providers) {
            System.out.println(provider.getName() + ": " + provider.getInfo());
        }
    }

    @Test
    void shouldListBcCapabilities() {
        Security.addProvider(new BouncyCastleProvider());

        Provider bc = Security.getProvider("BC");
        assertNotNull(bc);

        // 统计 BC Provider 提供的算法数量
        long algorithmCount = bc.keySet().stream()
                .filter(key -> key instanceof String s && !s.startsWith("Alg.Alias"))
                .count();

        // BC 提供数百种算法
        assertTrue(algorithmCount > 100);
        System.out.println("BC Provider 提供的算法数量: " + algorithmCount);
    }
}
