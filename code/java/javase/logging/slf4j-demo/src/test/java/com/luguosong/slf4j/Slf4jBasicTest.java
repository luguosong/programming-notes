package com.luguosong.slf4j;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SLF4J 日志门面基本用法演示
 *
 * <p>SLF4J（Simple Logging Facade for Java）是一个日志门面，
 * 业务代码只依赖 slf4j-api 接口，底层实现可自由切换。
 * 本示例使用 slf4j-simple 作为默认实现。</p>
 */
public class Slf4jBasicTest {

    // 通过 LoggerFactory 获取 Logger 实例，传入当前类的 Class 对象
    private static final Logger logger = LoggerFactory.getLogger(Slf4jBasicTest.class);

    /**
     * 基本日志输出 — 演示 SLF4J 的 5 个日志级别
     *
     * <p>slf4j-simple 默认级别为 INFO，因此 DEBUG 和 TRACE 不会输出。</p>
     */
    @Test
    void testBasicLogging() {
        // 从低到高：trace → debug → info → warn → error
        logger.trace("这是 TRACE 级别日志 — 最详细的调试信息");
        logger.debug("这是 DEBUG 级别日志 — 开发阶段的调试信息");
        logger.info("这是 INFO 级别日志 — 程序运行的关键节点信息");
        logger.warn("这是 WARN 级别日志 — 潜在问题警告");
        logger.error("这是 ERROR 级别日志 — 错误但程序可继续运行");

        // 验证 Logger 实例不为空
        assert logger != null;
    }

    /**
     * 参数化日志 — 使用 {} 占位符，避免不必要的字符串拼接
     *
     * <p>SLF4J 的占位符特性会在日志级别匹配时才进行字符串拼接，
     * 比字符串拼接（+）性能更好。</p>
     */
    @Test
    void testParameterizedLogging() {
        String username = "张三";
        String ip = "192.168.1.100";

        // 两个占位符：按顺序替换 {}
        logger.info("用户 {} 登录成功，IP: {}", username, ip);

        // 单个占位符
        int result = 42;
        logger.debug("计算结果: {}", result);

        // 验证日志输出
        assert logger.isInfoEnabled();
    }

    /**
     * 异常信息记录 — 异常作为最后一个参数
     *
     * <p>SLF4J 约定：异常对象必须作为最后一个参数传入，
     * 框架会自动输出完整的堆栈跟踪信息。</p>
     */
    @Test
    void testExceptionLogging() {
        try {
            // 模拟一个异常
            int[] numbers = {1, 2, 3};
            int value = numbers[10]; // 数组越界
        } catch (Exception e) {
            // 异常作为最后一个参数，SLF4J 会自动输出堆栈跟踪
            logger.error("操作失败", e);
        }
    }
}
