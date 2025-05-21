package com.luguosong.anno;

import org.springframework.stereotype.Component;

/**
 * @author luguosong
 */
@Component("userService")
public class UserServiceImpl {
    public void addUser() {
        System.out.println("添加用户");
    }

    public void deleteUser() {
        System.out.println("删除用户");
        //模拟异常
        int i = 1 / 0;
    }

    public void updateUser() {
        System.out.println("更新用户");
    }
}
