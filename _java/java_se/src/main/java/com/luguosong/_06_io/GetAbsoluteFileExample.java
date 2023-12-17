package com.luguosong._06_io;

import java.io.File;

/**
 * getAbsoluteFile使用场景
 *
 * @author luguosong
 */
public class GetAbsoluteFileExample {
    public static void main(String[] args) {
        File file = new File("hello.txt");

        // ❌由于Path使用的是相对路径，并不存在上层路径，所以返回null
        System.out.println(file.getParent()); //null

        // ✅使用getAbsoluteFile()方法，将path转为绝对路径，就可以获取到上层路径
        File absoluteFile = file.getAbsoluteFile();
        System.out.println(absoluteFile.getParent());
    }
}
