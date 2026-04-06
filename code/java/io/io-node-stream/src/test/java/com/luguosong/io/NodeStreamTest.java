package com.luguosong.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 演示节点流——直接连接数据源的底层流
 */
class NodeStreamTest {

    // --8<-- [start:file_input_stream_single_byte]
    /**
     * FileInputStream：一次读取一个字节
     */
    @Test
    void testFileInputStreamSingleByte() throws IOException {
        // 从类路径获取测试文件
        String path = getClass().getClassLoader()
                .getResource("test-read.txt").getPath();

        try (FileInputStream fis = new FileInputStream(path)) {
            int data;
            StringBuilder sb = new StringBuilder();
            // read() 返回 int（0~255），-1 表示读完
            while ((data = fis.read()) != -1) {
                sb.append((char) data);
            }
            System.out.println("逐字节读取结果: " + sb);
            assertTrue(sb.toString().contains("Hello"), "应包含 Hello");
            // ⚠️ 中文可能出现乱码，因为字节流不处理编码
        }
    }
    // --8<-- [end:file_input_stream_single_byte]

    // --8<-- [start:file_input_stream_byte_array]
    /**
     * FileInputStream：一次读取一个字节数组（推荐方式）
     */
    @Test
    void testFileInputStreamByteArray() throws IOException {
        String path = getClass().getClassLoader()
                .getResource("test-read.txt").getPath();

        try (FileInputStream fis = new FileInputStream(path)) {
            byte[] buffer = new byte[1024]; // 1KB 缓冲区
            int bytesRead;
            StringBuilder sb = new StringBuilder();
            while ((bytesRead = fis.read(buffer)) != -1) {
                // 注意用 bytesRead 控制实际读取长度
                sb.append(new String(buffer, 0, bytesRead));
            }
            System.out.println("数组读取结果: " + sb);
            assertTrue(sb.toString().contains("Hello"), "应包含 Hello");
        }
    }
    // --8<-- [end:file_input_stream_byte_array]

    // --8<-- [start:file_output_stream_write]
    /**
     * FileOutputStream：覆盖写入和追加写入
     */
    @Test
    void testFileOutputStreamWrite(@TempDir Path tempDir) throws IOException {
        File file = new File(tempDir + "/output.txt");

        // 覆盖写入（默认模式）
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write("Hello, IO!".getBytes());
        }

        // 追加写入（第二个参数为 true）
        try (FileOutputStream fos = new FileOutputStream(file, true)) {
            fos.write("\n追加的内容".getBytes());
        }

        // 验证文件内容
        try (FileInputStream fis = new FileInputStream(file)) {
            String content = new String(fis.readAllBytes());
            assertTrue(content.contains("Hello, IO!"), "应包含原始内容");
            assertTrue(content.contains("追加的内容"), "应包含追加内容");
            System.out.println("文件内容:\n" + content);
        }
    }
    // --8<-- [end:file_output_stream_write]

    // --8<-- [start:file_copy]
    /**
     * 实战：用字节流拷贝文件
     */
    @Test
    void testFileCopy(@TempDir Path tempDir) throws IOException {
        // 准备源文件
        File src = new File(tempDir + "/source.txt");
        try (FileOutputStream fos = new FileOutputStream(src)) {
            fos.write("这是要拷贝的内容，包含中文和 English".getBytes());
        }

        // 执行拷贝
        File dest = new File(tempDir + "/copy.txt");
        try (FileInputStream fis = new FileInputStream(src);
             FileOutputStream fos = new FileOutputStream(dest)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }

        // 验证拷贝结果
        assertTrue(dest.exists(), "目标文件应存在");
        assertEquals(src.length(), dest.length(), "文件大小应一致");
        System.out.println("拷贝成功，文件大小: " + dest.length() + " 字节");
    }
    // --8<-- [end:file_copy]

    // --8<-- [start:byte_array_stream]
    /**
     * ByteArrayInputStream / ByteArrayOutputStream：内存中的流
     */
    @Test
    void testByteArrayStream() throws IOException {
        // ByteArrayOutputStream：写入内存字节数组
        byte[] result;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            baos.write("Hello ".getBytes());
            baos.write("World".getBytes());
            result = baos.toByteArray();
        }
        assertEquals("Hello World", new String(result));

        // ByteArrayInputStream：从字节数组读取
        try (ByteArrayInputStream bais = new ByteArrayInputStream(result)) {
            int data;
            StringBuilder sb = new StringBuilder();
            while ((data = bais.read()) != -1) {
                sb.append((char) data);
            }
            assertEquals("Hello World", sb.toString());
        }
    }
    // --8<-- [end:byte_array_stream]

    // --8<-- [start:piped_stream]
    /**
     * PipedInputStream / PipedOutputStream：线程间通信
     */
    @Test
    void testPipedStream() throws IOException, InterruptedException {
        PipedInputStream pis = new PipedInputStream();
        PipedOutputStream pos = new PipedOutputStream();
        pis.connect(pos); // 建立管道连接

        String message = "Hello from another thread!";

        // 写入线程
        Thread writer = new Thread(() -> {
            try (pos) {
                pos.write(message.getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        writer.start();

        // 读取线程（当前线程）
        StringBuilder sb = new StringBuilder();
        try (pis) {
            int data;
            while ((data = pis.read()) != -1) {
                sb.append((char) data);
            }
        }

        writer.join(); // 等待写入线程结束
        assertEquals(message, sb.toString());
        System.out.println("管道流接收到: " + sb);
    }
    // --8<-- [end:piped_stream]
}
