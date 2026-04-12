package com.luguosong.log4j2;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Log4j2 基本用法演示
 */
class Log4j2BasicTest {

    /**
     * 使用 LogManager.getLogger() 获取 Log4j2 原生 Logger，输出各级别日志
     */
    @Test
    void testBasicLogging() {
        // Log4j2 使用自己的 API（不是 SLF4J），通过 LogManager 获取 Logger
        Logger logger = LogManager.getLogger(Log4j2BasicTest.class);
        assertNotNull(logger, "Logger 不应为 null");

        // Log4j2 支持的日志级别（比 SLF4J 多了 FATAL）
        logger.fatal("FATAL - 致命错误，程序可能无法继续运行");
        logger.error("ERROR - 错误信息，不影响程序继续运行");
        logger.warn("WARN - 警告信息，潜在的问题");
        logger.info("INFO - 一般信息");
        logger.debug("DEBUG - 调试信息");
        logger.trace("TRACE - 最详细的调试信息");
    }

    /**
     * 使用 {} 占位符的参数化日志
     */
    @Test
    void testParameterizedLogging() {
        Logger logger = LogManager.getLogger(Log4j2BasicTest.class);

        String username = "张三";
        String ip = "192.168.1.100";

        // Log4j2 也用 {} 占位符，语法与 SLF4J 一致
        logger.info("用户 {} 登录成功，IP: {}", username, ip);

        // 三个占位符示例
        logger.debug("处理订单：订单号={}, 金额={}, 状态={}", "ORD-001", 99.9, "已支付");
    }

    /**
     * 异步日志演示（全局异步模式）
     *
     * 启用方式：在 classpath 下放置 log4j2.component.properties 文件，
     * 内容为 Log4jContextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector
     *
     * 启用后所有 Logger 都自动变为异步，无需修改任何 Java 代码。
     * 底层依赖 LMAX Disruptor 实现超高性能。
     */
    @Test
    void testAsyncLogger() {
        Logger logger = LogManager.getLogger(Log4j2BasicTest.class);

        // 代码写法和同步模式完全一样，但底层已经是异步执行
        for (int i = 0; i < 100; i++) {
            logger.info("异步日志消息 #{}", i);
        }

        logger.info("异步日志演示完成");
    }
}
