package com.luguosong._06_io;

import java.io.*;
import java.util.Date;

/**
 * 使用数据流读写文件
 *
 * @author luguosong
 */
public class DataInputStreamAndOutputStream {
    public static void main(String[] args) {
        File file = new File("_java/java_se/src/main/resources/io/dataFile_" + new Date().getTime() + "_temp.txt");
        try {
            file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        DataInputStream dataInputStream = null;
        DataOutputStream dataOutputStream = null;

        /*
         * 创建数据流对象
         * */
        try {
            dataInputStream = new DataInputStream(new FileInputStream(file));
            dataOutputStream = new DataOutputStream(new FileOutputStream(file));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        /*
         * ⭐写文件
         * */
        try {
            dataOutputStream.writeUTF("字符串"); //写字符串
            dataOutputStream.writeInt(10); //写入基本数据类型
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                dataOutputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        /*
         * ⭐读文件
         * */
        try {
            System.out.println(dataInputStream.readUTF()); //读取字符串
            System.out.println(dataInputStream.readInt()); //读取基本数据类型
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                dataInputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
