package com.luguosong.ioc.create_bean;

import com.luguosong.ioc.User;

/**
 * @author luguosong
 */
public class FactoryMethod2 {
    public User getUser() {
        return new User(12, "lsg");
    }
}
