package com.luguosong._06_io;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * File类相关操作
 *
 * @author luguosong
 */
public class FileExample {
    public static void main(String[] args) throws IOException, InterruptedException {

        //*************** 🏷️创建File对象 ***************

        // 创建File对象
        File file1 = new File("_java/java_se/src/main/resources/io"); //文件夹
        File file2 = new File(file1, "helloTemp.txt"); //文件

        //*************** 🏷️文件创建 ***************
        if (!file1.exists())
            System.out.println("创建文件夹：" + file1.mkdirs());
        if (!file2.exists())
            System.out.println("创建文件：" + file2.createNewFile());
        Thread.sleep(3000); //暂停观察

        //*************** 🏷️文件操作 ***************
        File tempFile = new File(file1, "hello.txt");
        System.out.println("重命名，剪切：" + file2.renameTo(tempFile)); //重命名,也可以用于剪切文件
        file2=tempFile; //renameTo并不会改变file2的值，所以需要重新赋值
        Thread.sleep(3000); //暂停观察

        //*************** 🏷️获取文件基本信息 ***************
        System.out.println("绝对路径：" + file2.getAbsolutePath());
        System.out.println("路径：" + file2.getPath());
        System.out.println("文件名：" + file2.getName());
        System.out.println("转为绝对路径的File对象：" + file2.getAbsoluteFile().getPath());
        System.out.println("返回父目录路径：" + file2.getParent()); //需要path具有上层路径
        System.out.println("返回文件长度：" + file2.length());
        System.out.println("返回修改时间：" + file2.lastModified());

        System.out.println("返回目录下的文件列表：" + Arrays.toString(file1.list())); //返回文件名数组String[]
        System.out.println("返回目录下的文件列表：" + Arrays.toString(file1.listFiles())); //返回文件对象File[]

        System.out.println("判断文件是否存在：" + file2.exists());
        System.out.println("判断是否是文件：" + file2.isFile());
        System.out.println("判断是否是文件夹：" + file2.isDirectory());
        System.out.println("判断是否是隐藏文件：" + file2.isHidden());
        System.out.println("判断是否可读：" + file2.canRead());
        System.out.println("判断是否可写：" + file2.canWrite());

        //*************** 🏷️文件删除 ***************
        System.out.println("删除文件：" + file2.delete());

    }
}
