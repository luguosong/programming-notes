package com.luguosong._02_structural._07_proxy;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * cglib实现没有接口的代理
 * <p>
 * 在cglib代理中，不存在服务接口
 *
 * @author luguosong
 */
public class ProxyCglibExample {
    public static void main(String[] args) {
        // 创建Enhancer对象
        Enhancer enhancer = new Enhancer();
        // 设置目标类的父类
        enhancer.setSuperclass(TargetClass.class);
        // 设置回调对象
        enhancer.setCallback(new MethodInterceptor() {
            @Override
            public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
                System.out.println("前置增强");
                Object result = proxy.invokeSuper(obj, args);
                System.out.println("后置增强");
                return result;
            }
        });

        // 创建代理对象
        TargetClass proxy = (TargetClass) enhancer.create();

        // 调用代理对象的方法
        proxy.someMethod();
        proxy.someMethod2();
    }


    /**
     * 具体服务类
     */
    static class TargetClass {
        public void someMethod() {
            System.out.println("方法一");
        }

        public void someMethod2() {
            System.out.println("方法二");
        }
    }
}
