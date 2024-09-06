package com.luguosong.structural.proxy.static_proxy;

/**
 * @author luguosong
 */
public class Test {
    public static void main(String[] args) {
        UserService proxy = new UserServiceProxy(new UserServiceImpl());
        proxy.addUser();
    }
}
