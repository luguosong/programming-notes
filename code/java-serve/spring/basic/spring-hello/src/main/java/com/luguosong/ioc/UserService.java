package com.luguosong.ioc;

/**
 * @author luguosong
 */
public class UserService {

    private User user;

    public UserService() {
    }

    public UserService(User user) {
        this.user = user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void test() {
        System.out.println(user);
    }
}
