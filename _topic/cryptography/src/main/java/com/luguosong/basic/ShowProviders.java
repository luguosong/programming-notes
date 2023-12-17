package com.luguosong.basic;

import java.security.Provider;
import java.security.Security;

/**
 * @author luguosong
 */
public class ShowProviders {
    public static void main(String[] args) {
        // 获取系统中所有的服务提供者
        Provider[] providers = Security.getProviders();

        // 打印系统中所有的服务提供者
        for (Provider provider : providers) {
            System.out.println("*********************************************");
            System.out.println("服务提供者名称：" + provider.getName());
            System.out.println("服务提供者版本：" + provider.getVersion());
            //打印每个服务提供者支持的算法
            for (Object key : provider.keySet()) {
                System.out.println("key:" + key + ", 算法: " + provider.get(key));
            }
        }
    }
}
