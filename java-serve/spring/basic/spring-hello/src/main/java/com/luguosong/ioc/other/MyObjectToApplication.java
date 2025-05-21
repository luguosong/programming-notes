package com.luguosong.ioc.other;

import com.luguosong.ioc.User;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

/**
 * @author luguosong
 */
public class MyObjectToApplication {
    public static void main(String[] args) {
        User user = new User();
        //打印地址
        System.out.println(Integer.toHexString(System.identityHashCode(user))); //5b480cf9

        //将对象添加到容器中
        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
        factory.registerSingleton("user",user);

        //从容器中获取Bean
        User user1 = factory.getBean("user",User.class);
        System.out.println(Integer.toHexString(System.identityHashCode(user1))); //5b480cf9
    }
}
