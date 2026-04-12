package com.luguosong.log4j;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Log4j 1.x 基本用法演示
 */
class Log4jBasicTest {

    /**
     * 获取 Logger 并输出各级别日志
     */
    @Test
    void testBasicLogging() {
        // 通过类名获取 Logger（推荐方式）
        Logger logger = Logger.getLogger(Log4jBasicTest.class.getName());
        assertNotNull(logger, "Logger 不应为 null");

        // 从最严重到最轻微，依次输出各级别日志
        logger.fatal("FATAL - 致命错误，程序无法继续运行");
        logger.error("ERROR - 错误信息，不影响程序继续运行");
        logger.warn("WARN - 警告信息，潜在的问题");
        logger.info("INFO - 一般信息");
        logger.debug("DEBUG - 调试信息");
    }

    /**
     * 演示 PatternLayout 的格式化占位符
     */
    @Test
    void testPatternLayout() {
        // 使用 BasicConfigurator 进行最简配置（控制台输出，默认格式）
        BasicConfigurator.configure();
        Logger logger = Logger.getLogger("com.luguosong.log4j.pattern");

        // %d: 日期时间  %p: 级别  %c: Logger 名称  %m: 消息  %n: 换行  %L: 行号
        logger.info("PatternLayout 格式化演示");
        logger.debug("这条日志会显示日期、级别、Logger名称、行号等信息");

        // 验证日志级别优先级顺序
        assertTrue(Level.FATAL.toInt() > Level.ERROR.toInt(), "FATAL 应大于 ERROR");
        assertTrue(Level.ERROR.toInt() > Level.WARN.toInt(), "ERROR 应大于 WARN");
        assertTrue(Level.WARN.toInt() > Level.INFO.toInt(), "WARN 应大于 INFO");
        assertTrue(Level.INFO.toInt() > Level.DEBUG.toInt(), "INFO 应大于 DEBUG");
    }

    /**
     * 演示自定义 Logger，区别于 RootLogger
     */
    @Test
    void testCustomLogger() {
        // 自定义 Logger 名称，不使用类名
        Logger customLogger = Logger.getLogger("com.luguosong.custom");
        assertNotNull(customLogger, "自定义 Logger 不应为 null");

        // RootLogger 是所有 Logger 的祖先
        Logger rootLogger = Logger.getRootLogger();
        assertNotNull(rootLogger, "RootLogger 不应为 null");

        // 自定义 Logger 的日志也会传递给 RootLogger 处理
        customLogger.info("这条日志由自定义 Logger 输出");
        customLogger.warn("自定义 Logger 的警告信息");

        // 验证 Logger 名称
        assertEquals("com.luguosong.custom", customLogger.getName(),
                "Logger 名称应为 com.luguosong.custom");
        assertEquals("root", rootLogger.getName(),
                "RootLogger 名称应为 root");
    }
}
