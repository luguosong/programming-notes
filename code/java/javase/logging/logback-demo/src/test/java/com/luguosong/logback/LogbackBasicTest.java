package com.luguosong.logback;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Logback 基本用法演示
 */
class LogbackBasicTest {

    /**
     * 使用 LoggerFactory.getLogger() 获取 SLF4J Logger，输出各级别日志
     */
    @Test
    void testBasicLogging() {
        // Logback 原生实现 SLF4J，所以使用 SLF4J 的 API
        Logger logger = LoggerFactory.getLogger(LogbackBasicTest.class);
        assertNotNull(logger, "Logger 不应为 null");

        // 从最严重到最轻微，依次输出各级别日志
        logger.error("ERROR - 错误信息，不影响程序继续运行");
        logger.warn("WARN - 警告信息，潜在的问题");
        logger.info("INFO - 一般信息");
        logger.debug("DEBUG - 调试信息");
        logger.trace("TRACE - 最详细的调试信息");
    }

    /**
     * 使用 {} 占位符（SLF4J 风格的参数化日志）
     */
    @Test
    void testParameterizedLogging() {
        Logger logger = LoggerFactory.getLogger(LogbackBasicTest.class);

        String username = "张三";
        String ip = "192.168.1.100";

        // SLF4J 风格：用 {} 占位，避免字符串拼接开销
        logger.info("用户 {} 登录成功，IP: {}", username, ip);

        // 三个占位符示例
        logger.debug("处理订单：订单号={}, 金额={}, 状态={}", "ORD-001", 99.9, "已支付");
    }

    /**
     * 异常信息记录
     */
    @Test
    void testExceptionLogging() {
        Logger logger = LoggerFactory.getLogger(LogbackBasicTest.class);

        try {
            // 模拟一个异常
            int result = 10 / 0;
        } catch (Exception e) {
            // 将异常作为最后一个参数，Logback 会自动输出完整堆栈
            logger.error("操作失败", e);
        }

        // 也可以结合占位符使用
        try {
            String str = null;
            str.length();
        } catch (NullPointerException e) {
            logger.error("空指针异常，参数: {}", "str", e);
        }
    }

    /**
     * 使用 isDebugEnabled() 进行性能优化
     */
    @Test
    void testConditionalLogging() {
        Logger logger = LoggerFactory.getLogger(LogbackBasicTest.class);

        // 当日志级别高于 DEBUG 时，isDebugEnabled() 返回 false
        // 避免不必要的字符串拼接和对象创建
        if (logger.isDebugEnabled()) {
            // 只有 DEBUG 级别启用时才会执行这里的代码
            String expensiveInfo = "耗时: " + System.currentTimeMillis() + "ms";
            logger.debug("调试详情: {}", expensiveInfo);
        }

        // 对比：使用 {} 占位符时，SLF4J 内部也会判断级别
        // 但如果参数涉及昂贵运算（如 JSON 序列化），仍建议先用 isXxxEnabled() 判断
        logger.debug("当前时间戳: {}", System.currentTimeMillis());
    }
}
