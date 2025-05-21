package com.luguosong.ioc.dependency_injection;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author luguosong
 */
public class JdbcTest {
    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("ioc_jdbc.xml");
        Jdbc jdbc = context.getBean("jdbc", Jdbc.class);
        System.out.println(jdbc);
    }
}
