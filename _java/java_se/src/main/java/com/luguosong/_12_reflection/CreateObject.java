package com.luguosong._12_reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * 使用反射创建对象
 *
 * @author luguosong
 */
public class CreateObject {
    public static void main(String[] args) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Class<Person> personClass = Person.class;
        /*
        * 💀已弃用
        *
        * 使用newInstance必须满足以下条件：
        * 1.必须有公共的无参构造
        * 2.构造器的权限必须是public
        * */
        Person person1 = personClass.newInstance();

        /*
        * 先获取构造器，再创建对象
        *
        * 1.可以使用任意参数的构造器创建对象
        * 2.可以修改构造器访问权限
        * */
        Person person2 = personClass.getDeclaredConstructor().newInstance();

        Constructor<Person> constructor = personClass.getDeclaredConstructor(String.class);
        constructor.setAccessible(true); //改变私有构造器的权限
        Person person3 = constructor.newInstance("lisi");
    }
}

class Person {
    Person(){
        System.out.println("公共构造");
    }

    private Person(String name) {
        System.out.println("私有构造");
    }
}
