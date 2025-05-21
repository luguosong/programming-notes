package com.luguosong.ioc.dependency_injection;

import com.luguosong.ioc.User;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author luguosong
 */
public class IocUtilTest {
    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("ioc_util.xml");
        User user1 = context.getBean("user1", User.class);
        User user2 = context.getBean("user2", User.class);
        System.out.println(user1);
        System.out.println(user2);
    }
}
