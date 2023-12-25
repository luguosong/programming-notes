package com.luguosong._06_io;

import java.io.*;

/**
 * 使用转换流进行编码转换
 *
 * @author luguosong
 */
public class InputStreamReaderExample {
    public static void main(String[] args) throws IOException {
        /*
         * 使用文件流读取GBK编码的文件文件
         * 由于Charset.defaultCharset()是UTF-8,所以会出现乱码
         * */
        BufferedReader reader1 = new BufferedReader(new FileReader(new File("_java/java_se/src/main/resources/io/gbkFile.txt")));
        System.out.println(reader1.readLine()); // hello��������
        reader1.close();

        /*
         * 将字节流转换为字符流,并指定编码格式
         * */
        BufferedReader reader2 = new BufferedReader(
                new InputStreamReader(new FileInputStream(new File("_java/java_se/src/main/resources/io/gbkFile.txt")), "GBK")
        );
        System.out.println(reader2.readLine()); // hello，数据流
        reader2.close();
    }
}

/*
hello��������
hello，数据流
* */
