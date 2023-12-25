package com.luguosong._06_io;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * 使用common io库简化开发
 *
 * @author luguosong
 */
public class CommonIOExample {
    public static void main(String[] args) throws IOException {
        /*
        * 文件复制
        *
        * 使用common io库简化开发
        * */
        FileUtils.copyFile(
                new File("_java/java_se/src/main/resources/io/byteFile.png"),
                new File("_java/java_se/src/main/resources/io/byteFile_copy" + new Date().getTime() + ".png")
        );
    }
}
