package com.luguosong._06_io;

import java.io.*;

/**
 * 缓冲字符流读写文件
 *
 * @author luguosong
 */
public class BufferedReaderAndWriter {
    public static void main(String[] args) {
        /*
         * 创建文件
         * */
        File source = new File("_java/java_se/src/main/resources/io/charFile.txt").getAbsoluteFile();
        File target = new File("_java/java_se/src/main/resources/io/charFile_Copy_" + System.currentTimeMillis() + ".txt").getAbsoluteFile();


        FileReader fileReader = null;
        FileWriter fileWriter = null;
        BufferedReader bufferedReader = null;
        BufferedWriter bufferedWriter = null;

        /*
         * 创建IO对象
         * */
        try {
            fileReader = new FileReader(source);
            fileWriter = new FileWriter(target);
            bufferedReader = new BufferedReader(fileReader);
            bufferedWriter = new BufferedWriter(fileWriter);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        /*
         * 读写文件
         * */
        try {
            // 每次读一个字符数组
            char[] chars = new char[1024];
            //String s = bufferedReader.readLine(); //BufferedReader也可以直接读取一行
            int read1 = bufferedReader.read(chars); //返回读取的字符个数
            while (read1 != -1) {
                bufferedWriter.write(chars, 0, read1);
                //bufferedWriter.flush(); //手动写入文件
                read1 = bufferedReader.read(chars);  // 读取下一个字符数组
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                // 关闭资源
                bufferedReader.close();
                bufferedWriter.close();
                // 关闭外层流时,内层流也会自动关闭
            } catch (IOException e) {
                // 处理close的异常
                throw new RuntimeException(e);
            }
        }

    }
}
