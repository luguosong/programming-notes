package com.luguosong._06_io;

import java.io.*;
import java.util.Date;

/**
 * 缓冲字节流拷贝文件
 *
 * @author luguosong
 */
public class BufferedInputStreamAndOutPutStream {
    public static void main(String[] args) {
        // ********** ⭐创建File对象 **********
        File source = new File("_java/java_se/src/main/resources/io/byteFile.png");
        File target = new File("_java/java_se/src/main/resources/io/byteFile_Copy_" + new Date().getTime() + "_temp.png");

        // ********** ⭐创建缓冲字节流对象 **********
        FileInputStream fis = null;
        FileOutputStream fos = null;
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            fis = new FileInputStream(source);
            fos = new FileOutputStream(target);
            bis = new BufferedInputStream(fis);
            bos = new BufferedOutputStream(fos);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        // ********** ⭐文件复制,一次读取1024字节 **********
        try {
            byte[] bytes = new byte[1024];
            int len = 0;
            // 读取文件
            while ((len = bis.read(bytes)) != -1) {
                /* 写入文件
                 * */
                bos.write(bytes, 0, len);
                //bos.flush(); //手动写入文件
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            // 关闭资源
            try {
                bis.close();
                bos.close();
                // 关闭外层流时,内层流也会自动关闭
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
