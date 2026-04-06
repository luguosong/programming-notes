package com.luguosong.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 演示包装流——在节点流基础上叠加增强功能
 */
class WrapperStreamTest {

    // --8<-- [start:input_stream_reader]
    /**
     * 转换流：InputStreamReader 指定编码读取
     */
    @Test
    void testInputStreamReader(@TempDir Path tempDir) throws IOException {
        // 先用 UTF-8 编码写入文件
        File file = new File(tempDir + "/utf8.txt");
        try (OutputStreamWriter osw = new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8)) {
            osw.write("你好，世界！Hello World!");
        }

        // 用 InputStreamReader 指定 UTF-8 解码读取
        try (InputStreamReader isr = new InputStreamReader(
                new FileInputStream(file), StandardCharsets.UTF_8)) {
            char[] buffer = new char[1024];
            int charsRead = isr.read(buffer);
            String content = new String(buffer, 0, charsRead);
            System.out.println("转换流读取: " + content);
            assertTrue(content.contains("你好"), "应正确解码中文");
        }
    }
    // --8<-- [end:input_stream_reader]

    // --8<-- [start:file_reader_writer]
    /**
     * FileReader / FileWriter：转换流的简化版（使用平台默认编码）
     */
    @Test
    void testFileReaderWriter(@TempDir Path tempDir) throws IOException {
        File file = new File(tempDir + "/text.txt");

        // FileWriter 写入文本
        try (FileWriter fw = new FileWriter(file)) {
            fw.write("用 FileWriter 写入的文本\n");
            fw.write("第二行内容");
        }

        // FileReader 读取文本
        try (FileReader fr = new FileReader(file)) {
            char[] buffer = new char[1024];
            int charsRead = fr.read(buffer);
            String content = new String(buffer, 0, charsRead);
            System.out.println("FileReader 读取:\n" + content);
            assertTrue(content.contains("FileWriter"), "应包含写入内容");
        }
    }
    // --8<-- [end:file_reader_writer]

    // --8<-- [start:buffered_stream_copy]
    /**
     * 缓冲流：提升读写性能
     */
    @Test
    void testBufferedStreamCopy(@TempDir Path tempDir) throws IOException {
        // 准备源文件
        File src = new File(tempDir + "/source.dat");
        try (FileOutputStream fos = new FileOutputStream(src)) {
            byte[] data = new byte[10000]; // 10KB 测试数据
            for (int i = 0; i < data.length; i++) {
                data[i] = (byte) (i % 256);
            }
            fos.write(data);
        }

        // 用缓冲流拷贝（性能远优于裸 FileInputStream）
        File dest = new File(tempDir + "/copy.dat");
        try (BufferedInputStream bis = new BufferedInputStream(
                new FileInputStream(src));
             BufferedOutputStream bos = new BufferedOutputStream(
                     new FileOutputStream(dest))) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = bis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
        }

        assertEquals(src.length(), dest.length(), "拷贝后文件大小应一致");
        System.out.println("缓冲流拷贝完成，大小: " + dest.length() + " 字节");
    }
    // --8<-- [end:buffered_stream_copy]

    // --8<-- [start:buffered_reader_readline]
    /**
     * BufferedReader：readLine() 按行读取
     */
    @Test
    void testBufferedReaderReadLine() throws IOException {
        String path = getClass().getClassLoader()
                .getResource("lines.txt").getPath();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            int lineNumber = 0;
            while ((line = br.readLine()) != null) {
                lineNumber++;
                System.out.printf("第 %d 行: %s%n", lineNumber, line);
            }
            assertTrue(lineNumber > 0, "应至少读到一行");
        }
    }
    // --8<-- [end:buffered_reader_readline]

    // --8<-- [start:buffered_reader_mark_reset]
    /**
     * BufferedReader：mark() 和 reset() 实现回退读取
     */
    @Test
    void testBufferedReaderMarkReset() throws IOException {
        String path = getClass().getClassLoader()
                .getResource("lines.txt").getPath();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            br.mark(1024); // 标记当前位置
            String firstRead = br.readLine();
            System.out.println("第一次读: " + firstRead);

            br.reset(); // 回退到 mark 位置
            String secondRead = br.readLine();
            System.out.println("回退后再读: " + secondRead);

            // 两次读到的应该是同一行
            assertEquals(firstRead, secondRead, "reset 后应重新读取同一行");
        }
    }
    // --8<-- [end:buffered_reader_mark_reset]

    // --8<-- [start:data_stream]
    /**
     * 数据流：读写基本数据类型
     */
    @Test
    void testDataStream(@TempDir Path tempDir) throws IOException {
        File file = new File(tempDir + "/data.bin");

        // 写入基本数据类型
        try (DataOutputStream dos = new DataOutputStream(
                new FileOutputStream(file))) {
            dos.writeInt(42);
            dos.writeDouble(3.14);
            dos.writeBoolean(true);
            dos.writeUTF("Hello 数据流");
        }

        // 读取——顺序必须与写入一致！
        try (DataInputStream dis = new DataInputStream(
                new FileInputStream(file))) {
            int i = dis.readInt();
            double d = dis.readDouble();
            boolean b = dis.readBoolean();
            String s = dis.readUTF();

            assertEquals(42, i);
            assertEquals(3.14, d, 0.001);
            assertTrue(b);
            assertEquals("Hello 数据流", s);
            System.out.printf("读取结果: int=%d, double=%.2f, boolean=%s, string=%s%n",
                    i, d, b, s);
        }
    }
    // --8<-- [end:data_stream]

    // --8<-- [start:object_stream]
    /**
     * 对象流：序列化与反序列化 Java 对象
     */
    @Test
    void testObjectStream(@TempDir Path tempDir) throws IOException, ClassNotFoundException {
        File file = new File(tempDir + "/user.dat");

        // 序列化：将对象写入文件
        User user = new User("张三", 25, "secret123");
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(file))) {
            oos.writeObject(user);
        }

        // 反序列化：从文件还原对象
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(file))) {
            User restored = (User) ois.readObject();
            assertEquals("张三", restored.getName());
            assertEquals(25, restored.getAge());
            // transient 字段不参与序列化，还原后为 null
            assertNull(restored.getPassword(), "transient 字段应为 null");
            System.out.println("反序列化: " + restored);
        }
    }
    // --8<-- [end:object_stream]

    // --8<-- [start:print_stream]
    /**
     * 打印流：PrintStream 输出重定向
     */
    @Test
    void testPrintStream(@TempDir Path tempDir) throws IOException {
        File file = new File(tempDir + "/log.txt");

        // 保存原始 System.out
        PrintStream originalOut = System.out;

        try (PrintStream ps = new PrintStream(new FileOutputStream(file))) {
            // 将标准输出重定向到文件
            System.setOut(ps);
            System.out.println("这句话写入文件而非控制台");
            System.out.println(42);
            System.out.println(3.14);
        } finally {
            // 恢复原始 System.out
            System.setOut(originalOut);
        }

        // 验证文件内容
        try (FileInputStream fis = new FileInputStream(file)) {
            String content = new String(fis.readAllBytes());
            assertTrue(content.contains("这句话写入文件"), "日志应写入文件");
        }
        System.out.println("打印流重定向测试通过");
    }
    // --8<-- [end:print_stream]

    // --8<-- [start:gzip_stream]
    /**
     * 压缩流：GZIP 压缩与解压
     */
    @Test
    void testGzipStream(@TempDir Path tempDir) throws IOException {
        File gzFile = new File(tempDir + "/data.gz");
        String original = "这是一段需要压缩的文本数据。".repeat(100);

        // GZIP 压缩
        try (GZIPOutputStream gos = new GZIPOutputStream(
                new FileOutputStream(gzFile))) {
            gos.write(original.getBytes(StandardCharsets.UTF_8));
        }

        // GZIP 解压——先收集所有字节，再转字符串（避免 UTF-8 多字节字符在缓冲区边界被截断）
        ByteArrayOutputStream collector = new ByteArrayOutputStream();
        try (GZIPInputStream gis = new GZIPInputStream(
                new FileInputStream(gzFile))) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gis.read(buffer)) != -1) {
                collector.write(buffer, 0, len);
            }
        }
        String decompressed = collector.toString(StandardCharsets.UTF_8);

        assertEquals(original, decompressed, "解压后应与原文一致");
        System.out.printf("原始大小: %d 字节, 压缩后: %d 字节, 压缩比: %.1f%%%n",
                original.getBytes().length, gzFile.length(),
                (double) gzFile.length() / original.getBytes().length * 100);
    }
    // --8<-- [end:gzip_stream]

    // --8<-- [start:try_with_resources]
    /**
     * try-with-resources：自动关闭资源
     */
    @Test
    void testTryWithResources(@TempDir Path tempDir) throws IOException {
        File src = new File(tempDir + "/src.txt");
        File dest = new File(tempDir + "/dest.txt");

        // 先创建源文件
        try (FileWriter fw = new FileWriter(src)) {
            fw.write("try-with-resources 测试");
        }

        // 多个资源用分号分隔，按声明逆序关闭
        try (FileInputStream fis = new FileInputStream(src);
             FileOutputStream fos = new FileOutputStream(dest)) {
            fos.write(fis.readAllBytes());
        }
        // 离开 try 块后，fos 先关闭，fis 后关闭

        assertTrue(dest.exists(), "目标文件应存在");
        System.out.println("try-with-resources 自动关闭资源成功");
    }
    // --8<-- [end:try_with_resources]

    // --8<-- [start:properties_file_reader]
    /**
     * Properties 文件读取：方式一 Properties + FileReader
     */
    @Test
    void testPropertiesWithFileReader() throws IOException {
        String path = getClass().getClassLoader()
                .getResource("config.properties").getPath();

        Properties props = new Properties();
        try (FileReader fr = new FileReader(path)) {
            props.load(fr);
        }

        assertEquals("localhost", props.getProperty("db.host"));
        assertEquals("3306", props.getProperty("db.port"));
        // 提供默认值
        assertEquals("utf8mb4", props.getProperty("db.charset", "utf8mb4"));
        System.out.println("Properties 读取: " + props);
    }
    // --8<-- [end:properties_file_reader]

    // --8<-- [start:properties_resource_bundle]
    /**
     * Properties 文件读取：方式二 ResourceBundle（类路径）
     */
    @Test
    void testResourceBundle() {
        // 自动在 classpath 中查找 config.properties（不带后缀）
        ResourceBundle bundle = ResourceBundle.getBundle("config");

        assertEquals("localhost", bundle.getString("db.host"));
        assertEquals("testdb", bundle.getString("db.name"));
        System.out.println("ResourceBundle 读取 db.host: " + bundle.getString("db.host"));
    }
    // --8<-- [end:properties_resource_bundle]
}
