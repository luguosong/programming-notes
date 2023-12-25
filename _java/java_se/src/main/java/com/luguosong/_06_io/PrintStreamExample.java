package com.luguosong._06_io;

import java.io.PrintStream;

/**
 * @author luguosong
 */
public class PrintStreamExample {
    public static void main(String[] args) {
        PrintStream printStream = null;
        //输出到文件
        //printStream = new PrintStream("_java/java_se/src/main/resources/io/printStream_" + System.currentTimeMillis() + "_temp.txt");
        //输出到控制台
        printStream = new PrintStream(System.out);
        printStream.print("hello,");
        printStream.println("打印流");
        printStream.close();
    }
}
