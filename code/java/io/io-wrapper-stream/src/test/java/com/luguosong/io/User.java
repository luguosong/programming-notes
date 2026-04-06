package com.luguosong.io;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用于演示对象序列化的实体类
 */
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String name;
    private int age;
    // transient 字段不参与序列化
    private transient String password;

    public User(String name, int age, String password) {
        this.name = name;
        this.age = age;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "User{name='%s', age=%d, password='%s'}".formatted(name, age, password);
    }
}
