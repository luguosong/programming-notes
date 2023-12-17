package com.luguosong._12_reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * 使用反射调用指定属性、方法和构造器
 *
 * @author luguosong
 */
public class InvokeStructure {

    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<Person3> clazz = Person3.class;
        Constructor<Person3> constructor = clazz.getDeclaredConstructor();
        Person3 person = constructor.newInstance();


    }
}

class Person3 {

    private Integer age;

    public String name;

}
