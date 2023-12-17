package com.luguosong._12_reflection;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.*;
import java.util.Arrays;

/**
 * @author luguosong
 */
public class GetClassStructure {


    public static void main(String[] args) throws NoSuchFieldException {
        Class<Person2> clazz = Person2.class;

        /*
         * 获取公共属性
         * */
        System.out.println("==================获取公共属性==================");
        Field[] fields1 = clazz.getFields();
        for (Field field : fields1) {
            System.out.printf("权限：%s，类型：%s，名称：%s\n", Modifier.toString(field.getModifiers()), field.getType(), field.getName());
        }

        /*
         * 获取所有属性，包括私有属性
         * */
        System.out.println("==================获取所有属性，包括私有属性==================");
        Field[] fields2 = clazz.getDeclaredFields();
        for (Field field : fields2) {
            System.out.printf("权限：%s，类型：%s，名称：%s\n", Modifier.toString(field.getModifiers()), field.getType(), field.getName());
        }

        /*
         * 获取所有公共方法，包含父类的方法
         * */
        System.out.println("==================获取所有公共方法，包含父类的方法==================");
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            System.out.printf("权限：%s，返回值：%s，名称：%s\n", Modifier.toString(method.getModifiers()), method.getReturnType(), method.getName());
        }

        /*
         * 获取当前类中声明的方法
         * */
        System.out.println("==================获取当前类中声明的方法==================");
        Method[] methods2 = clazz.getDeclaredMethods();
        for (Method method : methods2) {
            System.out.printf("权限：%s，返回值：%s，名称：%s，注解：%s\n", Modifier.toString(method.getModifiers()), method.getReturnType(), method.getName(), Arrays.toString(method.getAnnotations()));


        }

        /*
         * 获取父类，以及父类所带的泛型
         * */
        System.out.println("==================获取父类信息==================");
        Class<? super Person2> superclass = clazz.getSuperclass(); //不带泛型的父类
        System.out.println(superclass);
        Type genericSuperclass = clazz.getGenericSuperclass(); //带泛型的父类
        System.out.println(genericSuperclass);
        ParameterizedType paramType = (ParameterizedType) genericSuperclass; //强转为带泛型的父类,❗只有在父类带泛型时才能进行强转
        Type[] arguments = paramType.getActualTypeArguments(); //获取泛型参数
        for (Type argument : arguments) {
            System.out.println(argument);
        }

        /*
         * 获取接口
         * */
        System.out.println("==================获取接口==================");
        Class<?>[] interfaces = clazz.getInterfaces();
        for (Class<?> anInterface : interfaces) {
            System.out.println(anInterface);
        }

        /*
         * 获取类所在包
         * */
        System.out.println("==================获取类所在包==================");
        Package aPackage = clazz.getPackage();
        System.out.println(aPackage);

    }
}

interface Person2Interface {
    public void sayHello();
}

class Person2Parent<String> {
    public String parent;
}

class Person2 extends Person2Parent<String> implements Person2Interface {
    public String name;

    public static String address;

    private Integer age;


    private void sayHi() {
        System.out.println("Hi");
    }

    @Override
    @Person2sayHello
    public void sayHello() {
        System.out.println("Hello");
    }
}

/**
 * 只有在运行时才能获取到注解信息
 */
@Retention(RetentionPolicy.RUNTIME)
@interface Person2sayHello {

}

