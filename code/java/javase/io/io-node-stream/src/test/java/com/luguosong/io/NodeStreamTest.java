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

    // --8<-- [start:char_array_reader_writer]
    /**
     * CharArrayReader / CharArrayWriter：内存中的字符流
     */
    @Test
    void testCharArrayReaderWriter() throws IOException {
        // CharArrayWriter：写入内存字符数组
        char[] result;
        try (CharArrayWriter caw = new CharArrayWriter()) {
            caw.write("你好，");
            caw.write("世界！");
            result = caw.toCharArray();
        }
        assertEquals("你好，世界！", new String(result));

        // CharArrayReader：从字符数组读取
        try (CharArrayReader car = new CharArrayReader(result)) {
            StringBuilder sb = new StringBuilder();
            int ch;
            while ((ch = car.read()) != -1) {
                sb.append((char) ch);
            }
            assertEquals("你好，世界！", sb.toString());
        }
    }
    // --8<-- [end:char_array_reader_writer]

    // --8<-- [start:string_reader_writer]
    /**
     * StringReader / StringWriter：以 String 为数据源的字符流
     */
    @Test
    void testStringReaderWriter() throws IOException {
        // StringReader：从字符串读取
        String source = "Java IO 流是个大家族\n字节流和字符流各有用武之地";
        try (StringReader sr = new StringReader(source);
             BufferedReader br = new BufferedReader(sr)) {
            // 包装成 BufferedReader 后可以按行读取
            assertEquals("Java IO 流是个大家族", br.readLine());
            assertEquals("字节流和字符流各有用武之地", br.readLine());
            assertNull(br.readLine()); // 没有更多内容
        }

        // StringWriter：写入后得到字符串
        try (StringWriter sw = new StringWriter()) {
            sw.write("第一部分");
            sw.write(" + ");
            sw.write("第二部分");
            assertEquals("第一部分 + 第二部分", sw.toString());
        }
    }
    // --8<-- [end:string_reader_writer]

    // --8<-- [start:random_access_file]
    /**
     * RandomAccessFile：随机读写文件
     */
    @Test
    void testRandomAccessFile(@TempDir Path tempDir) throws IOException {
        File file = tempDir.resolve("random.dat").toFile();

        // 写入数据：3 个 int（每个占 4 字节）
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            raf.writeInt(100);  // 位置 0~3
            raf.writeInt(200);  // 位置 4~7
            raf.writeInt(300);  // 位置 8~11
            raf.writeUTF("你好"); // 位置 12 开始，writeUTF 会先写 2 字节长度
        }

        // 随机读取：直接跳到第 2 个 int 的位置
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            raf.seek(4); // 跳过第 1 个 int（4 字节），定位到第 2 个
            assertEquals(200, raf.readInt());

            // 回到开头读取第 1 个
            raf.seek(0);
            assertEquals(100, raf.readInt());

            // 跳到第 3 个
            raf.seek(8);
            assertEquals(300, raf.readInt());

            // 读取字符串
            assertEquals("你好", raf.readUTF());
        }

        // 随机修改：只改第 2 个 int，其他不动
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            raf.seek(4);
            raf.writeInt(999); // 覆盖位置 4~7 的数据
        }

        // 验证修改结果
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            assertEquals(100, raf.readInt());  // 第 1 个未变
            assertEquals(999, raf.readInt());  // 第 2 个已修改
            assertEquals(300, raf.readInt());  // 第 3 个未变
        }
    }
    // --8<-- [end:random_access_file]

    // --8<-- [start:sequence_input_stream]
    /**
     * SequenceInputStream：将多个流串联成一个流
     */
    @Test
    void testSequenceInputStream() throws IOException {
        InputStream s1 = new ByteArrayInputStream("Hello ".getBytes());
        InputStream s2 = new ByteArrayInputStream("World".getBytes());
        InputStream s3 = new ByteArrayInputStream("!".getBytes());

        // 方式一：串联两个流
        try (SequenceInputStream sis = new SequenceInputStream(s1, s2)) {
            byte[] result = sis.readAllBytes();
            assertEquals("Hello World", new String(result));
        }

        // 方式二：串联多个流（通过 Enumeration）
        java.util.Vector<InputStream> streams = new java.util.Vector<>();
        streams.add(new ByteArrayInputStream("A".getBytes()));
        streams.add(new ByteArrayInputStream("B".getBytes()));
        streams.add(new ByteArrayInputStream("C".getBytes()));

        try (SequenceInputStream sis = new SequenceInputStream(streams.elements())) {
            assertEquals("ABC", new String(sis.readAllBytes()));
        }
    }
    // --8<-- [end:sequence_input_stream]
}
