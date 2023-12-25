package com.luguosong._06_io;

import java.io.*;

/**
 * 文件字符流读写操作
 *
 * @author luguosong
 */
public class FileReaderAndWriter {
    public static void main(String[] args) {
        /*
         * 创建文件
         * */
        File source = new File("_java/java_se/src/main/resources/io/charFile.txt").getAbsoluteFile();
        File target = new File("_java/java_se/src/main/resources/io/charFile_Copy_" + System.currentTimeMillis() + ".txt").getAbsoluteFile();


        FileReader fileReader = null;
        FileWriter fileWriter = null;

        /*
         * 创建IO对象
         * */
        try {
            fileReader = new FileReader(source);
            fileWriter = new FileWriter(target);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        /*
         * 读写文件
         * */
        try {
            // 每次读一个字符数组
            char[] chars = new char[1024];
            int read1 = fileReader.read(chars); //返回读取的字符个数
            while (read1 != -1) {
                fileWriter.write(chars, 0, read1);
                read1 = fileReader.read(chars);  // 读取下一个字符数组
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                // 关闭资源
                fileReader.close();
                fileWriter.close();
            } catch (IOException e) {
                // 处理close的异常
                throw new RuntimeException(e);
            }
        }
    }
}
