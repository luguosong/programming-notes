package com.luguosong.ioc.create_bean;

import com.luguosong.ioc.User;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author luguosong
 */
public class FactoryMethod2Test {
    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("ioc_create_factory_method2.xml");
        //通过工厂方法创建对象
        User user = context.getBean("user", User.class);
        System.out.println(user);
    }
}
