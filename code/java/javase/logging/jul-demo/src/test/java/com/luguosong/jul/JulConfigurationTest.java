package com.luguosong.jul;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.logging.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUL 配置方式演示
 */
class JulConfigurationTest {

    /**
     * 通过 Java 代码配置 Logger
     */
    @Test
    void testJavaCodeConfiguration() {
        Logger logger = Logger.getLogger("jul.demo.config.code");

        // 屏蔽父 Handler，避免日志重复输出
        logger.setUseParentHandlers(false);

        // 设置 Logger 级别为 ALL（输出所有级别）
        logger.setLevel(Level.ALL);

        // 创建 ConsoleHandler 并设置级别和格式化器
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.ALL);
        consoleHandler.setFormatter(new SimpleFormatter());

        // 将 Handler 添加到 Logger
        logger.addHandler(consoleHandler);

        // 输出各级别日志，验证配置生效
        logger.severe("Java 代码配置 - SEVERE 级别");
        logger.info("Java 代码配置 - INFO 级别");
        logger.fine("Java 代码配置 - FINE 级别（默认不显示，现在应该能看到）");

        // 验证 Handler 数量
        assertEquals(1, logger.getHandlers().length, "应有 1 个 Handler");
        assertTrue(logger.getHandlers()[0] instanceof ConsoleHandler);

        // 清理：移除 Handler
        logger.removeHandler(consoleHandler);
        assertEquals(0, logger.getHandlers().length, "移除后应无 Handler");
    }

    /**
     * 同时配置 ConsoleHandler + FileHandler
     */
    @Test
    void testMultipleHandlers() throws IOException {
        Logger logger = Logger.getLogger("jul.demo.config.multi");
        logger.setUseParentHandlers(false);
        logger.setLevel(Level.ALL);

        // 控制台 Handler
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.INFO);
        consoleHandler.setFormatter(new SimpleFormatter());

        // 文件 Handler（输出到临时目录）
        String tempDir = System.getProperty("java.io.tmpdir");
        File logFile = new File(tempDir, "jul-demo-multi.log");
        FileHandler fileHandler = new FileHandler(logFile.getAbsolutePath(), true);
        fileHandler.setLevel(Level.ALL);
        fileHandler.setFormatter(new SimpleFormatter());

        // 同时添加两个 Handler
        logger.addHandler(consoleHandler);
        logger.addHandler(fileHandler);

        // 输出日志：控制台只显示 INFO 及以上，文件记录所有级别
        logger.info("这条日志会同时输出到控制台和文件");
        logger.fine("这条日志只会输出到文件（控制台级别不够）");

        // 验证两个 Handler 都存在
        assertEquals(2, logger.getHandlers().length, "应有 2 个 Handler");

        // 清理资源
        fileHandler.close();
        logger.removeHandler(consoleHandler);
        logger.removeHandler(fileHandler);

        System.out.println("日志文件路径: " + logFile.getAbsolutePath());
    }

    /**
     * 通过 LogManager.readConfiguration() 加载自定义配置文件
     */
    @Test
    void testCustomPropertiesFile() throws IOException {
        // 创建自定义 logging.properties 内容
        String properties = """
                # 自定义 JUL 配置
                handlers=java.util.logging.ConsoleHandler
                .level=INFO

                # ConsoleHandler 配置
                java.util.logging.ConsoleHandler.level=ALL
                java.util.logging.ConsoleHandler.formatter=java.util.logging.SimpleFormatter

                # 自定义 Logger 级别
                jul.demo.config.properties.level=ALL
                """;

        // 将配置写入临时文件并加载
        File tempProps = File.createTempFile("logging", ".properties");
        java.nio.file.Files.writeString(tempProps.toPath(), properties);

        try {
            // 加载自定义配置
            LogManager.getLogManager().readConfiguration(tempProps.toURI().toURL().openStream());

            Logger logger = Logger.getLogger("jul.demo.config.properties");
            logger.setUseParentHandlers(false);

            // 重新配置 Handler（readConfiguration 后需要重新添加）
            ConsoleHandler handler = new ConsoleHandler();
            handler.setLevel(Level.ALL);
            logger.addHandler(handler);

            logger.info("使用自定义 properties 配置输出");
            logger.fine("自定义配置允许 FINE 级别输出");

            System.out.println("自定义配置文件路径: " + tempProps.getAbsolutePath());
        } finally {
            tempProps.deleteOnExit();
        }
    }
}
