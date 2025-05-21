package com.luguosong;

import jakarta.servlet.http.HttpSessionBindingEvent;
import jakarta.servlet.http.HttpSessionBindingListener;

/**
 * @author luguosong
 */
public class User implements HttpSessionBindingListener {
    @Override
    public void valueBound(HttpSessionBindingEvent event) {
        System.out.println("User对象被放入到session域中");
    }

    @Override
    public void valueUnbound(HttpSessionBindingEvent event) {
        System.out.println("");
    }

    private String name;
    private Integer age;

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
