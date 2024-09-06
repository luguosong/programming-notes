package com.luguosong.ioc.life_cycle;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author luguosong
 */
public class LifeCycleTest {
    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("ioc_life_cycle.xml");
        context.getBean("dog", Dog.class);
        context.close();
    }
}
