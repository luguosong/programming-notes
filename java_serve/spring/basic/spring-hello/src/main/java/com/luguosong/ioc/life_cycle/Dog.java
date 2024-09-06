package com.luguosong.ioc.life_cycle;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.*;

/**
 * @author luguosong
 */
public class Dog implements BeanNameAware, BeanClassLoaderAware, BeanFactoryAware , InitializingBean,DisposableBean {

    private String name;

    public Dog() {
        System.out.println("实例化Bean");
    }

    public void setName(String name) {
        System.out.println("调用setter方法属性赋值");
        this.name = name;
    }

    @Override
    public void setBeanName(String name) {
        System.out.println("实现BeanNameAware接口，setBeanName方法执行");
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        System.out.println("实现BeanClassLoaderAware接口，setBeanClassLoader方法执行");
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        System.out.println("实现BeanFactoryAware接口，setBeanFactory方法执行");
    }

    public void initBean() {
        System.out.println("初始化Bean");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("实现InitializingBean接口，afterPropertiesSet方法执行");
    }

    @Override
    public void destroy() throws Exception {
        System.out.println("实现DisposableBean接口，destroy方法执行");
    }

    public void destroyBean() {
        System.out.println("销毁Bean");
    }

}
