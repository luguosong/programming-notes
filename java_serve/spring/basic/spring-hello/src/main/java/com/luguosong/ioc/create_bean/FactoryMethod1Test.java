package com.luguosong.ioc.create_bean;

import com.luguosong.ioc.User;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author luguosong
 */
public class FactoryMethod1Test {
    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("ioc_create_factory_method1.xml");
        //通过工厂方法创建对象
        User user = context.getBean("userFactory", User.class);
        System.out.println(user);
    }
}
