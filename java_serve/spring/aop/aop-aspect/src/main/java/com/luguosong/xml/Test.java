package com.luguosong.xml;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author luguosong
 */
public class Test {
    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("aspect.xml");
        UserServiceImpl userService = context.getBean("userService",UserServiceImpl.class);
        userService.addUser();
    }
}
