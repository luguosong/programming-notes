package com.luguosong.ioc.dependency_injection;

import com.luguosong.ioc.UserService;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author luguosong
 */
public class IocAutoTest {
    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("ioc_auto.xml");
        UserService service1 = context.getBean("userService1", UserService.class);
        service1.test();

        UserService service2 = context.getBean("userService2", UserService.class);
        service2.test();
    }
}
