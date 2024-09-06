package com.luguosong.structural.proxy.static_proxy;

/**
 * @author luguosong
 */
public class UserServiceProxy implements UserService{

    private UserService userService;

    public UserServiceProxy(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void addUser() {
        System.out.println("前置增强");
        userService.addUser();
        System.out.println("后置增强");
    }

    @Override
    public void editUser() {
        System.out.println("前置增强");
        userService.editUser();
        System.out.println("后置增强");
    }

    @Override
    public void deleteUser() {
        System.out.println("前置增强");
        userService.deleteUser();
        System.out.println("后置增强");
    }
}
