package com.luguosong.jul;

import org.junit.jupiter.api.Test;

import java.text.MessageFormat;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUL 基本用法演示
 */
class JulBasicTest {

    /**
     * 获取 Logger 对象并验证非空
     */
    @Test
    void testGetLogger() {
        // 通过类名获取 Logger（推荐方式）
        Logger logger = Logger.getLogger(JulBasicTest.class.getName());
        assertNotNull(logger, "Logger 不应为 null");

        // 通过自定义名称获取 Logger
        Logger customLogger = Logger.getLogger("com.luguosong.custom");
        assertNotNull(customLogger, "自定义 Logger 不应为 null");

        // 相同名称返回同一个实例
        Logger sameLogger = Logger.getLogger(JulBasicTest.class.getName());
        assertSame(logger, sameLogger, "相同名称应返回同一个 Logger 实例");

        System.out.println("Logger 名称: " + logger.getName());
    }

    /**
     * 依次输出各级别日志
     */
    @Test
    void testLogLevel() {
        Logger logger = Logger.getLogger("jul.demo.loglevel");

        // 将级别设为 ALL，确保所有日志都能输出
        logger.setLevel(java.util.logging.Level.ALL);
        // 移除默认的父 Handler，添加自己的 ConsoleHandler 以显示全部级别
        logger.setUseParentHandlers(false);
        java.util.logging.ConsoleHandler handler = new java.util.logging.ConsoleHandler();
        handler.setLevel(java.util.logging.Level.ALL);
        logger.addHandler(handler);

        // 从最严重到最轻微，依次输出各级别日志
        logger.severe("SEVERE - 严重错误，程序可能无法继续运行");
        logger.warning("WARNING - 警告信息，潜在的问题");
        logger.info("INFO - 一般信息（JUL 默认级别）");
        logger.config("CONFIG - 配置信息");
        logger.fine("FINE - 调试信息（详细级别）");
        logger.finer("FINER - 更详细的调试信息");
        logger.finest("FINEST - 最详细的调试信息");

        // 验证日志级别数值：数值越大越严重
        assertTrue(java.util.logging.Level.SEVERE.intValue() > java.util.logging.Level.WARNING.intValue(),
                "SEVERE 数值应大于 WARNING");
    }

    /**
     * 使用 MessageFormat 进行占位符日志输出
     */
    @Test
    void testPlaceholder() {
        Logger logger = Logger.getLogger("jul.demo.placeholder");

        // 方式一：字符串拼接（简单直接，但性能较差）
        String user = "张三";
        int age = 25;
        logger.info("用户信息: name=" + user + ", age=" + age);

        // 方式二：String.format（C 风格格式化）
        logger.info(String.format("用户 %s 的年龄是 %d 岁", user, age));

        // 方式三：MessageFormat.format（JUL 推荐方式）
        // 使用 {0}, {1}, ... 占位符
        logger.info(MessageFormat.format("用户 {0} 的年龄是 {1} 岁", user, age));

        // 方式四：Logger.log() 方法支持传入参数数组（内部使用 MessageFormat）
        logger.log(java.util.logging.Level.INFO,
                "登录成功: 用户 {0}，年龄 {1} 岁",
                new Object[]{user, age});

        System.out.println("占位符日志输出完成");
    }
}
