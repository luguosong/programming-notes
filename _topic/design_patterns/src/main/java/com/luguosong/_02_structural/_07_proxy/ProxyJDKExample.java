package com.luguosong._02_structural._07_proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * JDK动态代理
 *
 * @author luguosong
 */
public class ProxyJDKExample {
    public static void main(String[] args) {
        RealService realService = new RealService();
        /*
         * 动态创建代理对象
         * */
        ServiceInterface proxyObject = (ServiceInterface) Proxy.newProxyInstance(
                realService.getClass().getClassLoader(),
                realService.getClass().getInterfaces(),
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        System.out.println("代理前置增强");
                        //执行具体服务的方法
                        Object invoke = method.invoke(realService, args);
                        System.out.println("代理后置增强");
                        return invoke;
                    }
                }
        );

        /*
        * 执行代理对象
        *
        * JDK代理最终是对服务接口进行代理
        * */
        proxyObject.operation();
    }

    // 服务接口（Service Interface）：定义了服务和代理的共同操作
    static interface ServiceInterface {
        void operation();
    }

    // 具体服务（Service）：执行实际的操作
    static class RealService implements ServiceInterface {
        public void operation() {
            System.out.println("在实际服务中进行操作。");
        }
    }
}




