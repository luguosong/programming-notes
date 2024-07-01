package com.luguosong.structural.proxy.dynamic_proxy_cglib;

import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * @author luguosong
 */
public class Test {
    public static void main(String[] args) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(UserServiceImpl.class);

        enhancer.setCallback(new MethodInterceptor() {
            @Override
            public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
                System.out.println("前置增强");
                Object ret = proxy.invokeSuper(obj, args);
                System.out.println("后置增强");
                return ret;
            }
        });

        UserServiceImpl userService = (UserServiceImpl)enhancer.create();
        userService.addUser();

    }
}
