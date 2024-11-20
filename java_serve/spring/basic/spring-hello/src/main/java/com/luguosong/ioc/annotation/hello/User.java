package com.luguosong.ioc.annotation.hello;

import org.springframework.stereotype.Component;

/**
 * 如果不指定value，则默认使用类名首字母小写作为id名
 * @author luguosong
 */
@Component("user")
public class User {
    private String name;

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                '}';
    }
}
