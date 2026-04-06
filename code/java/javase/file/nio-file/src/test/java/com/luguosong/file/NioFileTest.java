package com.luguosong.file;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 演示 NIO.2 的 Path 与 Files 工具类
 */
class NioFileTest {

    // --8<-- [start:path_create]
    /**
     * 创建 Path 对象的多种方式
     */
    @Test
    void testPathCreation() {
        // 方式一：Path.of()（Java 11+，推荐）
        Path p1 = Path.of("docs", "java", "index.md");

        // 方式二：Paths.get()（Java 7+，等价于 Path.of）
        Path p2 = Paths.get("docs", "java", "index.md");

        // 两种方式结果相同
        assertEquals(p1, p2);

        System.out.println("路径: " + p1);
        System.out.println("文件名: " + p1.getFileName());
        System.out.println("父路径: " + p1.getParent());
        System.out.println("路径片段数: " + p1.getNameCount());
    }
    // --8<-- [end:path_create]

    // --8<-- [start:path_operations]
    /**
     * Path 的路径拼接与解析
     */
    @Test
    void testPathOperations() {
        Path base = Path.of("project", "src");

        // resolve()：拼接子路径
        Path full = base.resolve("main").resolve("java");
        assertEquals(Path.of("project", "src", "main", "java"), full);

        // resolveSibling()：替换最后一个片段（取兄弟路径）
        Path sibling = full.resolveSibling("resources");
        assertEquals(Path.of("project", "src", "main", "resources"), sibling);

        // relativize()：计算两个路径的相对关系
        Path from = Path.of("project", "src");
        Path to = Path.of("project", "docs", "guide");
        Path relative = from.relativize(to);
        System.out.println("从 src 到 docs/guide 的相对路径: " + relative);

        // normalize()：消除 . 和 .. 冗余片段
        Path messy = Path.of("project", "src", "..", "docs", ".", "guide");
        Path clean = messy.normalize();
        assertEquals(Path.of("project", "docs", "guide"), clean);
    }
    // --8<-- [end:path_operations]

    // --8<-- [start:path_info]
    /**
     * 获取 Path 的各种属性信息
     */
    @Test
    void testPathInfo(@TempDir Path tempDir) throws IOException {
        // 创建一个测试文件
        Path file = tempDir.resolve("info-test.txt");
        Files.writeString(file, "Hello NIO.2");

        // 基本路径信息
        System.out.println("文件名: " + file.getFileName());
        System.out.println("父路径: " + file.getParent());
        System.out.println("根路径: " + file.getRoot());
        System.out.println("是绝对路径: " + file.isAbsolute());

        // 通过 Files 工具类获取文件属性
        System.out.println("存在: " + Files.exists(file));
        System.out.println("是普通文件: " + Files.isRegularFile(file));
        System.out.println("是目录: " + Files.isDirectory(file));
        System.out.println("可读: " + Files.isReadable(file));
        System.out.println("可写: " + Files.isWritable(file));
        System.out.println("文件大小: " + Files.size(file) + " 字节");

        assertTrue(Files.exists(file));
        assertTrue(Files.isRegularFile(file));
    }
    // --8<-- [end:path_info]

    // --8<-- [start:files_read_write]
    /**
     * Files 工具类的一行式读写
     */
    @Test
    void testFilesReadWrite(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("demo.txt");

        // ✅ 一行写入字符串（Java 11+）
        Files.writeString(file, "你好，NIO.2！\n这是第二行。");

        // ✅ 一行读取全部内容为字符串（Java 11+）
        String content = Files.readString(file);
        assertTrue(content.contains("你好，NIO.2！"));

        // ✅ 按行读取为 List<String>（Java 7+）
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        assertEquals(2, lines.size());
        assertEquals("你好，NIO.2！", lines.get(0));
        assertEquals("这是第二行。", lines.get(1));

        // ✅ 按行写入 List<String>（Java 7+）
        Path file2 = tempDir.resolve("lines.txt");
        Files.write(file2, List.of("第一行", "第二行", "第三行"));
        assertEquals(3, Files.readAllLines(file2).size());

        // ✅ 写入字节数组（Java 7+）
        Path binFile = tempDir.resolve("data.bin");
        byte[] data = {0x48, 0x65, 0x6C, 0x6C, 0x6F}; // "Hello"
        Files.write(binFile, data);
        assertArrayEquals(data, Files.readAllBytes(binFile));
    }
    // --8<-- [end:files_read_write]

    // --8<-- [start:files_lines_stream]
    /**
     * Files.lines() —— 流式懒加载按行读取（适合大文件）
     */
    @Test
    void testFilesLinesStream(@TempDir Path tempDir) throws IOException {
        // 准备测试文件
        Path file = tempDir.resolve("large.txt");
        Files.write(file, List.of(
                "// 这是注释行",
                "name=Alice",
                "// 另一条注释",
                "age=30",
                "city=Shanghai"
        ));

        // Files.lines() 返回 Stream<String>，惰性读取，用完即关
        try (Stream<String> stream = Files.lines(file)) {
            List<String> configs = stream
                    .filter(line -> !line.startsWith("//")) // 跳过注释
                    .collect(Collectors.toList());

            assertEquals(3, configs.size());
            assertEquals("name=Alice", configs.get(0));
        }
        // Stream 关闭后底层文件句柄自动释放
    }
    // --8<-- [end:files_lines_stream]

    // --8<-- [start:files_dir_create]
    /**
     * 目录的创建
     */
    @Test
    void testDirectoryCreation(@TempDir Path tempDir) throws IOException {
        // createDirectory()：创建单级目录（父目录必须存在）
        Path single = tempDir.resolve("level1");
        Files.createDirectory(single);
        assertTrue(Files.isDirectory(single));

        // createDirectories()：创建多级目录（推荐，自动创建中间目录）
        Path multi = tempDir.resolve("a").resolve("b").resolve("c");
        Files.createDirectories(multi);
        assertTrue(Files.isDirectory(multi));

        // createTempDirectory()：创建临时目录
        Path tmpDir = Files.createTempDirectory(tempDir, "test_");
        assertTrue(Files.isDirectory(tmpDir));
        assertTrue(tmpDir.getFileName().toString().startsWith("test_"));

        // createTempFile()：创建临时文件
        Path tmpFile = Files.createTempFile(tempDir, "pre_", ".tmp");
        assertTrue(Files.exists(tmpFile));
        assertTrue(tmpFile.getFileName().toString().endsWith(".tmp"));
    }
    // --8<-- [end:files_dir_create]

    // --8<-- [start:files_copy_move_delete]
    /**
     * 文件的复制、移动与删除
     */
    @Test
    void testCopyMoveDelete(@TempDir Path tempDir) throws IOException {
        // 准备源文件
        Path src = tempDir.resolve("source.txt");
        Files.writeString(src, "原始内容");

        // 复制文件
        Path copied = tempDir.resolve("copied.txt");
        Files.copy(src, copied);
        assertEquals("原始内容", Files.readString(copied));

        // 复制并覆盖已存在的目标文件
        Files.writeString(copied, "旧内容");
        Files.copy(src, copied, StandardCopyOption.REPLACE_EXISTING);
        assertEquals("原始内容", Files.readString(copied));

        // 移动（重命名）文件
        Path moved = tempDir.resolve("moved.txt");
        Files.move(copied, moved);
        assertFalse(Files.exists(copied), "原文件应已不存在");
        assertTrue(Files.exists(moved), "新位置应存在");

        // 删除文件
        Files.delete(moved);
        assertFalse(Files.exists(moved));

        // deleteIfExists 不抛异常（文件不存在时返回 false）
        boolean deleted = Files.deleteIfExists(moved);
        assertFalse(deleted, "文件已不存在，应返回 false");
    }
    // --8<-- [end:files_copy_move_delete]

    // --8<-- [start:files_walk]
    /**
     * Files.walk() —— 递归遍历目录树
     */
    @Test
    void testFilesWalk(@TempDir Path tempDir) throws IOException {
        // 构建目录结构：
        // tempDir/
        //   ├── a/
        //   │   ├── a1.txt
        //   │   └── a2.java
        //   ├── b/
        //   │   └── b1.txt
        //   └── root.txt
        Files.createDirectories(tempDir.resolve("a"));
        Files.createDirectories(tempDir.resolve("b"));
        Files.writeString(tempDir.resolve("root.txt"), "根文件");
        Files.writeString(tempDir.resolve("a/a1.txt"), "a1");
        Files.writeString(tempDir.resolve("a/a2.java"), "a2");
        Files.writeString(tempDir.resolve("b/b1.txt"), "b1");

        // walk() 递归遍历所有文件和目录
        try (Stream<Path> stream = Files.walk(tempDir)) {
            long totalCount = stream.count();
            // tempDir 本身 + a/ + b/ + 4个文件 = 7
            assertEquals(7, totalCount);
        }

        // 只查找 .txt 文件
        try (Stream<Path> stream = Files.walk(tempDir)) {
            List<String> txtFiles = stream
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".txt"))
                    .map(p -> p.getFileName().toString())
                    .sorted()
                    .collect(Collectors.toList());

            assertEquals(List.of("a1.txt", "b1.txt", "root.txt"), txtFiles);
        }

        // walk(maxDepth)：限制遍历深度
        try (Stream<Path> stream = Files.walk(tempDir, 1)) {
            long topLevelCount = stream
                    .filter(p -> !p.equals(tempDir)) // 排除根目录自身
                    .count();
            // 只有 a/, b/, root.txt 三项（不进入子目录）
            assertEquals(3, topLevelCount);
        }
    }
    // --8<-- [end:files_walk]

    // --8<-- [start:files_walk_file_tree]
    /**
     * Files.walkFileTree() —— 访问者模式遍历（可做删除等复杂操作）
     */
    @Test
    void testWalkFileTree(@TempDir Path tempDir) throws IOException {
        // 构建嵌套目录
        Path dir = tempDir.resolve("toDelete");
        Files.createDirectories(dir.resolve("sub1").resolve("sub2"));
        Files.writeString(dir.resolve("file.txt"), "内容");
        Files.writeString(dir.resolve("sub1/file.txt"), "内容");
        Files.writeString(dir.resolve("sub1/sub2/file.txt"), "内容");

        assertTrue(Files.exists(dir));

        // 使用 walkFileTree + SimpleFileVisitor 递归删除整个目录树
        Files.walkFileTree(dir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException {
                Files.delete(file); // 先删文件
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path directory, IOException exc)
                    throws IOException {
                Files.delete(directory); // 再删空目录
                return FileVisitResult.CONTINUE;
            }
        });

        assertFalse(Files.exists(dir), "整个目录树应被删除");
    }
    // --8<-- [end:files_walk_file_tree]

    // --8<-- [start:path_matcher]
    /**
     * PathMatcher —— 用 glob 模式查找文件
     */
    @Test
    void testPathMatcher(@TempDir Path tempDir) throws IOException {
        // 准备测试文件
        Files.writeString(tempDir.resolve("readme.md"), "文档");
        Files.writeString(tempDir.resolve("App.java"), "代码");
        Files.writeString(tempDir.resolve("test.txt"), "文本");
        Files.writeString(tempDir.resolve("data.csv"), "数据");

        // 创建 glob 匹配器：匹配 .java 或 .md 文件
        PathMatcher matcher = FileSystems.getDefault()
                .getPathMatcher("glob:*.{java,md}");

        try (Stream<Path> stream = Files.list(tempDir)) {
            List<String> matched = stream
                    .filter(p -> matcher.matches(p.getFileName()))
                    .map(p -> p.getFileName().toString())
                    .sorted()
                    .collect(Collectors.toList());

            assertEquals(List.of("App.java", "readme.md"), matched);
        }
    }
    // --8<-- [end:path_matcher]

    // --8<-- [start:file_vs_path]
    /**
     * File 与 Path 的相互转换
     */
    @Test
    void testFilePathConversion(@TempDir Path tempDir) throws IOException {
        // Path → File
        Path path = tempDir.resolve("convert.txt");
        Files.writeString(path, "转换测试");
        java.io.File file = path.toFile();
        assertTrue(file.exists());

        // File → Path
        Path backToPath = file.toPath();
        assertEquals(path, backToPath);
        assertEquals("转换测试", Files.readString(backToPath));
    }
    // --8<-- [end:file_vs_path]

    // --8<-- [start:watch_service]
    /**
     * WatchService：监控目录变化
     */
    @Test
    void testWatchService(@TempDir Path tempDir) throws IOException, InterruptedException {
        // 创建 WatchService 并注册要监控的目录
        try (WatchService watcher = FileSystems.getDefault().newWatchService()) {
            tempDir.register(watcher,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_DELETE);

            // 在被监控的目录中创建一个文件
            Path newFile = tempDir.resolve("hello.txt");
            Files.writeString(newFile, "WatchService 演示");

            // 等待事件（poll 最多等 2 秒）
            WatchKey key = watcher.poll(2, TimeUnit.SECONDS);
            assertNotNull(key, "应该收到文件创建事件");

            boolean foundCreate = false;
            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();
                Path fileName = (Path) event.context();
                System.out.println("事件: " + kind.name() + " -> " + fileName);
                if (kind == StandardWatchEventKinds.ENTRY_CREATE
                        && fileName.toString().equals("hello.txt")) {
                    foundCreate = true;
                }
            }
            assertTrue(foundCreate, "应该检测到 hello.txt 的创建事件");

            // 重置 key 以继续监听（不重置则不会收到后续事件）
            key.reset();
        }
    }
    // --8<-- [end:watch_service]
}
