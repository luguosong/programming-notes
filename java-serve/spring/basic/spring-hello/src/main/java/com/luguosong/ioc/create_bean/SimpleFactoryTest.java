package com.luguosong.ioc.create_bean;

import com.luguosong.ioc.User;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author luguosong
 */
public class SimpleFactoryTest {
    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("ioc_create_simple_factory.xml");
        User user = context.getBean("user", User.class);
        System.out.println(user);
    }
}
