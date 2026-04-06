package com.luguosong.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 演示 NIO（New I/O）的 Channel + Buffer 模型
 */
class NioTest {

    // --8<-- [start:bytebuffer_basics]
    /**
     * ByteBuffer 基本操作：allocate / put / flip / get / clear
     */
    @Test
    void testByteBufferBasics() {
        // 分配一个容量为 16 字节的缓冲区
        ByteBuffer buf = ByteBuffer.allocate(16);
        // 初始状态：position=0, limit=capacity=16
        assertEquals(0, buf.position());
        assertEquals(16, buf.limit());
        assertEquals(16, buf.capacity());

        // 写入数据
        buf.put((byte) 'H');
        buf.put((byte) 'i');
        assertEquals(2, buf.position()); // position 移动到 2

        // flip()：切换为读模式（limit=position, position=0）
        buf.flip();
        assertEquals(0, buf.position());
        assertEquals(2, buf.limit()); // limit 变为之前写入的位置

        // 读取数据
        assertEquals('H', (char) buf.get());
        assertEquals('i', (char) buf.get());
        assertFalse(buf.hasRemaining()); // 已读完

        // clear()：重置为写模式（position=0, limit=capacity）
        buf.clear();
        assertEquals(0, buf.position());
        assertEquals(16, buf.limit());
    }
    // --8<-- [end:bytebuffer_basics]

    // --8<-- [start:filechannel_write_read]
    /**
     * FileChannel + ByteBuffer：读写文件
     */
    @Test
    void testFileChannelWriteRead(@TempDir Path tempDir) throws IOException {
        File file = tempDir.resolve("nio-test.txt").toFile();
        String content = "你好，NIO！";

        // 写入：FileOutputStream → FileChannel → ByteBuffer
        try (FileChannel fc = new FileOutputStream(file).getChannel()) {
            ByteBuffer buf = ByteBuffer.wrap(content.getBytes(StandardCharsets.UTF_8));
            fc.write(buf);
        }

        // 读取：FileInputStream → FileChannel → ByteBuffer
        try (FileChannel fc = new FileInputStream(file).getChannel()) {
            ByteBuffer buf = ByteBuffer.allocate((int) fc.size());
            fc.read(buf);     // 数据写入 buffer
            buf.flip();       // 切换为读模式
            String result = StandardCharsets.UTF_8.decode(buf).toString();
            assertEquals(content, result);
        }
    }
    // --8<-- [end:filechannel_write_read]

    // --8<-- [start:channel_transfer]
    /**
     * transferTo：通道间直接传输（零拷贝文件复制）
     */
    @Test
    void testChannelTransfer(@TempDir Path tempDir) throws IOException {
        File src = tempDir.resolve("src.txt").toFile();
        File dest = tempDir.resolve("dest.txt").toFile();

        // 准备源文件
        try (FileOutputStream fos = new FileOutputStream(src)) {
            fos.write("Channel transfer demo".getBytes(StandardCharsets.UTF_8));
        }

        // 通道间直接传输，无需中间 Buffer
        try (FileChannel inCh = new FileInputStream(src).getChannel();
             FileChannel outCh = new FileOutputStream(dest).getChannel()) {
            inCh.transferTo(0, inCh.size(), outCh);
        }

        // 验证
        try (FileChannel fc = new FileInputStream(dest).getChannel()) {
            ByteBuffer buf = ByteBuffer.allocate((int) fc.size());
            fc.read(buf);
            buf.flip();
            assertEquals("Channel transfer demo",
                    StandardCharsets.UTF_8.decode(buf).toString());
        }
    }
    // --8<-- [end:channel_transfer]

    // --8<-- [start:memory_mapped_file]
    /**
     * 内存映射文件：把文件当数组操作
     */
    @Test
    void testMemoryMappedFile() throws IOException {
        // MappedByteBuffer 在 Windows 上会锁定文件直到 GC，因此不使用 @TempDir
        File file = File.createTempFile("nio-mapped-", ".dat");
        file.deleteOnExit();
        int size = 1024; // 映射 1KB

        // 写入：通过内存映射直接操作文件
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            MappedByteBuffer mapped = raf.getChannel()
                    .map(FileChannel.MapMode.READ_WRITE, 0, size);

            // 像操作数组一样写入数据
            for (int i = 0; i < 26; i++) {
                mapped.put((byte) ('A' + i));
            }
            // 强制将修改刷到磁盘
            mapped.force();
        }

        // 读取验证（不使用映射，避免再次锁定文件）
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] data = fis.readNBytes(26);
            assertEquals('A', (char) data[0]);
            assertEquals('Z', (char) data[25]);
        }

        assertEquals(size, file.length());
    }
    // --8<-- [end:memory_mapped_file]

    // --8<-- [start:bytebuffer_view]
    /**
     * 视图缓冲区：通过不同视角操作 ByteBuffer
     */
    @Test
    void testByteBufferView() {
        ByteBuffer bb = ByteBuffer.allocate(16);

        // 通过 IntBuffer 视图写入 int 值（每个 int 占 4 字节）
        bb.asIntBuffer().put(new int[]{42, 100, 999, -1});

        // 通过 IntBuffer 视图读取
        assertEquals(42, bb.getInt(0));   // 位置 0
        assertEquals(100, bb.getInt(4));  // 位置 4
        assertEquals(999, bb.getInt(8));  // 位置 8
        assertEquals(-1, bb.getInt(12));  // 位置 12

        // 直接读取原始字节也能看到数据
        assertEquals(16, bb.limit());
    }
    // --8<-- [end:bytebuffer_view]
}
