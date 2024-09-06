package com.luguosong.structural.proxy.dynamic_proxy_jdk;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author luguosong
 */
public class Test {
    public static void main(String[] args) {
        UserService service = new UserServiceImpl();

        UserService serviceProxy = (UserService) Proxy.newProxyInstance(service.getClass().getClassLoader(),
                service.getClass().getInterfaces(),
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        System.out.println("前置增强");
                        Object ret = method.invoke(service, args);
                        System.out.println("后置增强");
                        return ret;
                    }
                });

        serviceProxy.addUser();
    }
}
