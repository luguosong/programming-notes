package com.luguosong._12_reflection;

import java.util.Date;

/**
 * @author luguosong
 */
public class GetClass {
    public static void main(String[] args) throws ClassNotFoundException {
        // 方式一：通过类的静态属性获取
        Class<Date> class1 = Date.class;

        // 方式二：通过类的实例对象获取
        Date date = new Date();
        Class<? extends Date> class2 = date.getClass();

        // 方式三：通过类的全限定名获取

        Class<?> class3 = Class.forName("java.util.Date");

        // 方式四：通过类加载器获取
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        Class<?> class4 = classLoader.loadClass("java.util.Date");
    }
}
