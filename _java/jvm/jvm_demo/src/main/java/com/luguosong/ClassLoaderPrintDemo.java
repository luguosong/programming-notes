package com.luguosong;

import sun.misc.Launcher;

import java.net.URL;

/**
 * 打印各个类加载器
 *
 * @author luguosong
 */
public class ClassLoaderPrintDemo {
    public static void main(String[] args) {
        ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
        System.out.println(systemClassLoader); //sun.misc.Launcher$AppClassLoader@18b4aac2

        ClassLoader extClassLoader = systemClassLoader.getParent();
        System.out.println(extClassLoader); //sun.misc.Launcher$ExtClassLoader@1b6d3586


        ClassLoader bootstrapClassLoader = extClassLoader.getParent();
        System.out.println(bootstrapClassLoader); //null
        System.out.println("打印bootstrapClassLoader加载的jar包：");
        for (URL e : Launcher.getBootstrapClassPath().getURLs()) {
            System.out.println(e.toExternalForm());
        }
    }
}
