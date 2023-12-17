package com.luguosong._06_io;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 文件字符流读写操作
 *
 * @author luguosong
 */
public class FileReaderAndWriter {
    public static void main(String[] args) {
        // ********** ⭐创建文件 **********
        File source = new File("_java/java_se/src/main/resources/io/charFile.txt").getAbsoluteFile();
        File target = new File("_java/java_se/src/main/resources/io/charFile_Copy_" + System.currentTimeMillis() + ".txt").getAbsoluteFile();



        FileReader fileReader = null;
        FileWriter fileWriter = null;
        try {
            // 每次读一个字符数组
            fileReader = new FileReader(source);
            fileWriter = new FileWriter(target);
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
                if (fileReader != null) {
                    fileReader.close();
                }
                if (fileWriter != null) {
                    fileWriter.close();
                }
            } catch (IOException e) {
                // 处理close的异常
                throw new RuntimeException(e);
            }
        }
    }
}
