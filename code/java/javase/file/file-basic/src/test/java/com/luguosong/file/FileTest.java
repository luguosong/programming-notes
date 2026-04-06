package com.luguosong.file;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 演示 java.io.File 类的常用操作
 */
class FileTest {

    // --8<-- [start:create_file_object]
    /**
     * 创建 File 对象的三种方式
     */
    @Test
    void testCreateFileObject(@TempDir Path tempDir) {
        // 方式一：通过路径字符串创建
        File file1 = new File(tempDir + "/hello.txt");

        // 方式二：通过父目录字符串 + 子路径创建
        File file2 = new File(tempDir.toString(), "hello.txt");

        // 方式三：通过父 File 对象 + 子路径创建
        File parent = tempDir.toFile();
        File file3 = new File(parent, "hello.txt");

        // 三种方式指向同一个路径
        assertEquals(file1.getAbsolutePath(), file2.getAbsolutePath());
        assertEquals(file2.getAbsolutePath(), file3.getAbsolutePath());
    }
    // --8<-- [end:create_file_object]

    // --8<-- [start:file_create_and_delete]
    /**
     * 文件的创建、判断与删除
     */
    @Test
    void testFileCreateAndDelete(@TempDir Path tempDir) throws IOException {
        File file = new File(tempDir + "/test.txt");

        // 文件尚未创建
        assertFalse(file.exists(), "文件不应存在");

        // 创建文件
        boolean created = file.createNewFile();
        assertTrue(created, "文件应创建成功");
        assertTrue(file.exists(), "文件应已存在");
        assertTrue(file.isFile(), "应为文件而非目录");

        // 获取文件名和绝对路径
        assertEquals("test.txt", file.getName());
        System.out.println("绝对路径: " + file.getAbsolutePath());

        // 删除文件
        boolean deleted = file.delete();
        assertTrue(deleted, "文件应删除成功");
        assertFalse(file.exists(), "文件应已不存在");
    }
    // --8<-- [end:file_create_and_delete]

    // --8<-- [start:directory_operations]
    /**
     * 目录的创建与遍历
     */
    @Test
    void testDirectoryOperations(@TempDir Path tempDir) throws IOException {
        // mkdir 只能创建单级目录
        File singleDir = new File(tempDir + "/level1");
        assertTrue(singleDir.mkdir(), "单级目录应创建成功");
        assertTrue(singleDir.isDirectory(), "应为目录");

        // mkdirs 可以创建多级目录（推荐）
        File multiDir = new File(tempDir + "/a/b/c");
        assertTrue(multiDir.mkdirs(), "多级目录应创建成功");

        // 在目录中创建几个文件
        new File(singleDir, "file1.txt").createNewFile();
        new File(singleDir, "file2.txt").createNewFile();
        new File(singleDir, "sub").mkdir();

        // listFiles() 列出目录内容
        File[] files = singleDir.listFiles();
        assertNotNull(files);
        assertEquals(3, files.length, "应有 2 个文件 + 1 个子目录");

        for (File f : files) {
            System.out.printf("%-10s | 是文件: %s | 是目录: %s%n",
                    f.getName(), f.isFile(), f.isDirectory());
        }
    }
    // --8<-- [end:directory_operations]

    // --8<-- [start:classpath_resource]
    /**
     * 获取类路径资源
     */
    @Test
    void testClasspathResource() {
        // 获取类路径根目录的绝对路径
        String classpath = Thread.currentThread()
                .getContextClassLoader()
                .getResource("")
                .getPath();

        System.out.println("类路径根目录: " + classpath);
        assertNotNull(classpath, "类路径不应为空");
    }
    // --8<-- [end:classpath_resource]
}
