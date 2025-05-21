package com.luguosong.ioc.create_bean;

import com.luguosong.ioc.User;

/**
 * 通过简单工厂创建bean
 *
 * @author luguosong
 */
public class SimpleFactory {
    //定义静态方法用于创建对象
    public static User getUser() {
        return new User(12,"李四");
    }
}
