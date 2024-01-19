package com.luguosong.xml_ioc;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author luguosong
 */
public class IocTest {
    public static void main(String[] args) {
        // 创建 IOC 容器
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("spring-config1.xml");

        //getBean有多种重载方法，这里使用的是根据bean的id获取bean的方法
        Component component1 = applicationContext.getBean("component1", Component.class);
        component1.method();
    }
}
