package com.luguosong.ioc.annotation.dependency_injection;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author luguosong
 */
public class Test {
    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("ioc_annotation_dependency_injection .xml");
        User user = context.getBean("user", User.class);
        user.play();
        user.driver();
        System.out.println(user);
    }
}
