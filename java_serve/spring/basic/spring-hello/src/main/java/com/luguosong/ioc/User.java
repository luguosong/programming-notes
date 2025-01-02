package com.luguosong.ioc;

import java.util.Arrays;
import java.util.Map;

/**
 * @author luguosong
 */
public class User {
    private String name;
    private Integer age;
    private String[] hobby;
    private Map<String, String> additionalInfo;

    public User() {

    }

    public User(Integer age, String name) {
        this.age = age;
        this.name = name;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setHobby(String[] hobby) {
        this.hobby = hobby;
    }

    public void setAdditionalInfo(Map<String, String> additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    @Override
    public String toString() {
        return "User{" +
                "additionalInfo=" + additionalInfo +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", hobby=" + Arrays.toString(hobby) +
                '}';
    }
}
