package com.luguosong.ioc.annotation.hello;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author luguosong
 */
public class Test {
    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("ioc_annotation_hello.xml");
        User user = context.getBean("user", User.class);
        System.out.println(user);
    }
}
