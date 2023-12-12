package com.luguosong;

import sun.security.ec.CurveDB;

/**
 * 观察各种常见的类使用什么类加载器
 *
 * @author luguosong
 */
public class ClassLoaderDemo {
    public static void main(String[] args) {
        // 用户自定义的应用程序中的类使用系统类加载器
        System.out.println(ClassLoaderDemo.class.getClassLoader()); //sun.misc.Launcher$AppClassLoader@18b4aac2

        // CurveDB是扩展类加载器加载的
        System.out.println(CurveDB.class.getClassLoader()); //sun.misc.Launcher$ExtClassLoader@4b67cf4d

        //String属于Java核心库，使用引导类加载器加载
        System.out.println(String.class.getClassLoader()); //null
    }
}
