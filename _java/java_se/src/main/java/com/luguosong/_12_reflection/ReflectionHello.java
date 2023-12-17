package com.luguosong._12_reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author luguosong
 */
public class ReflectionHello {

    /**
     * 自定义Person类
     */
    private static class Person {
        public Person() {
            name = "张三";
            age = 18;
            System.out.println("Person类的无参构造方法");
        }

        private Person(String name, Integer age) {
            this.name = name;
            this.age = age;
            System.out.println("Person类的私有有参构造方法");
        }

        public String name;
        private Integer age; //私有属性

        public void hello() {
            System.out.println("hello");
        }

        private void hello(String name) {
            System.out.println("hello " + name);
        }
    }

    public static void main(String[] args) throws InstantiationException, IllegalAccessException, NoSuchFieldException, NoSuchMethodException, InvocationTargetException {
        //使用一般方法创建对象并调用方法
        Person person = new Person();
        System.out.println(person.name);
        person.hello();

        //*****************************************************

        // 获取Class对象
        Class<Person> clazz = Person.class;

        //*****************************************************

        // 反射通过公共构造创建对象
        Person person1 = (Person) clazz.newInstance(); //使用反射创建对象

        // 反射可以调用私有的构造方法
        Constructor<Person> constructor = clazz.getDeclaredConstructor(String.class, Integer.class);
        constructor.setAccessible(true);
        Person person2 = constructor.newInstance("李四", 20);

        //*****************************************************

        // 反射访问公共成员
        Field name = clazz.getField("name");
        System.out.println(name.get(person1));

        // 反射直接访问私有成员
        Field age = clazz.getDeclaredField("age");
        age.setAccessible(true);
        System.out.println(age.get(person2));

        //*****************************************************

        // 反射访问公共方法
        clazz.getMethod("hello").invoke(person1);

        // 反射可以调用私有的方法
        Method method = clazz.getDeclaredMethod("hello", String.class);
        method.setAccessible(true);
        method.invoke(person2, "王五");
    }
}
