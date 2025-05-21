package com.luguosong.anno;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author luguosong
 */
public class Test {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(SpringConfig.class);
        UserServiceImpl userService = context.getBean("userService", UserServiceImpl.class);
        userService.addUser();
        userService.deleteUser();
    }
}
