package com.luguosong.structural.proxy.static_proxy;

/**
 * @author luguosong
 */
public class UserServiceImpl implements UserService{
    @Override
    public void addUser() {
        System.out.println("添加用户");
    }

    @Override
    public void editUser() {
        System.out.println("修改用户");
    }

    @Override
    public void deleteUser() {
        System.out.println("删除用户");
    }
}
