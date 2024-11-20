package com.luguosong.ioc.annotation.config;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author luguosong
 */
public class Test {
    public static void main(String[] args) {
        // 根据配置类创建Spring容器
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(SpringConfig.class);
        User user = context.getBean("user", User.class);
        System.out.println(user);
    }
}
