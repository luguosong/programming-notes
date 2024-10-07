package com.luguosong.structural.proxy.dynamic_proxy_cglib;

import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;

/**
 * @author luguosong
 */
public class Test {
    public static void main(String[] args) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(UserServiceImpl.class);

        enhancer.setCallback((MethodInterceptor) (obj, method, args1, proxy) -> {
            System.out.println("前置增强");
            Object ret = proxy.invokeSuper(obj, args1);
            System.out.println("后置增强");
            return ret;
        });

        UserServiceImpl userService = (UserServiceImpl)enhancer.create();
        userService.addUser();

    }
}
