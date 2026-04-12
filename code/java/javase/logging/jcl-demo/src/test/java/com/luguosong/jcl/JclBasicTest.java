package com.luguosong.jcl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;

/**
 * JCL（Apache Commons Logging）基本用法演示
 *
 * <p>JCL 是一个日志门面（Logging Facade），本身不提供日志实现。
 * 它通过 LogFactory 的自动发现机制，在运行时查找可用的日志实现。
 * 当仅有 JCL 依赖时，默认使用 JCL 自带的 SimpleLog（基于 JUL）。</p>
 */
public class JclBasicTest {

    /**
     * 基本日志输出 — 默认使用 JCL 自带的 SimpleLog（底层基于 JUL）
     */
    @Test
    void testBasicLoggingWithJul() {
        // 通过 LogFactory 获取日志记录器，传入当前类的 Class 对象
        Log log = LogFactory.getLog(JclBasicTest.class);

        // JCL 默认使用 SimpleLog，其日志级别由系统属性 org.apache.commons.logging.log 控制
        // 默认级别为 INFO，因此 DEBUG 和 TRACE 不会输出
        log.trace("这是 TRACE 级别日志 — 默认不输出");
        log.debug("这是 DEBUG 级别日志 — 默认不输出");
        log.info("这是 INFO 级别日志");
        log.warn("这是 WARN 级别日志");
        log.error("这是 ERROR 级别日志");
        log.fatal("这是 FATAL 级别日志");

        // 验证日志对象不为空
        assert log != null;
    }

    /**
     * 演示 JCL 支持的全部 6 个日志级别方法
     */
    @Test
    void testAllLogLevelMethods() {
        Log log = LogFactory.getLog(JclBasicTest.class);

        // 从低到高：trace → debug → info → warn → error → fatal
        log.trace("TRACE — 最详细的调试信息");
        log.debug("DEBUG — 开发阶段的调试信息");
        log.info("INFO — 程序运行的关键节点信息");
        log.warn("WARN — 潜在问题警告");
        log.error("ERROR — 错误但程序可继续运行");
        log.fatal("FATAL — 严重错误，程序可能终止");

        // 每个级别都有对应的布尔判断方法，可用于避免不必要的字符串拼接
        if (log.isDebugEnabled()) {
            log.debug("昂贵操作的结果: " + expensiveOperation());
        }
    }

    /**
     * 演示 LogFactory 的自动发现机制 — 打印实际的 Log 实现类
     */
    @Test
    void testLogFactoryDiscovery() {
        Log log = LogFactory.getLog(JclBasicTest.class);

        // 打印 Log 实例的实际类名，观察 JCL 选择了哪个实现
        System.out.println("Log 实现类: " + log.getClass().getName());
        System.out.println("LogFactory 实现类: " + LogFactory.getFactory().getClass().getName());

        // 仅引入 commons-logging 时，默认使用 JCL 自带的 SimpleLog
        // 添加 Log4j 依赖后，会自动切换为 Log4j 的实现
        log.info("当前日志实现: " + log.getClass().getSimpleName());
    }

    /**
     * 模拟一个耗资源的操作，用于演示 isDebugEnabled() 的使用场景
     */
    private String expensiveOperation() {
        return "计算结果: " + System.currentTimeMillis();
    }
}
