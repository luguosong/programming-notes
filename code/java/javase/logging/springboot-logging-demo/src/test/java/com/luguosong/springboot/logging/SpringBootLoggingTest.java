package com.luguosong.springboot.logging;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Spring Boot 日志配置演示测试
 * <p>
 * 验证 Spring Boot 默认日志体系（SLF4J + Logback）
 * 以及 application.yml 中各项日志配置的效果
 */
@SpringBootTest
public class SpringBootLoggingTest {

    // 通过 SLF4J 门面获取 Logger，不依赖具体实现
    private static final Logger logger = LoggerFactory.getLogger(SpringBootLoggingTest.class);

    /**
     * 在 Spring Boot 环境中使用 LoggerFactory.getLogger() 输出各级别日志，
     * 验证 SLF4J + Logback 组合在默认配置下的行为
     */
    @Test
    void testDefaultLogging() {
        // 各级别日志输出
        logger.trace("这是一条 TRACE 级别日志");
        logger.debug("这是一条 DEBUG 级别日志");
        logger.info("这是一条 INFO 级别日志");
        logger.warn("这是一条 WARN 级别日志");
        logger.error("这是一条 ERROR 级别日志");

        // 占位符用法
        String user = "张三";
        logger.info("用户 {} 登录成功", user);

        // 异常日志
        logger.error("操作失败", new RuntimeException("模拟业务异常"));
    }

    /**
     * 验证不同 profile 下日志行为差异
     * <p>
     * 默认 profile（dev）：root 级别 INFO，com.luguosong 包级别 DEBUG
     * prod profile：root 级别 WARN，同时输出到文件
     */
    @Test
    void testProfileLogging() {
        // 当前默认 profile 下，com.luguosong 包级别为 DEBUG
        // 因此 DEBUG 及以上级别日志会被输出
        logger.debug("默认 profile 下，DEBUG 级别可见");
        logger.info("默认 profile 下，INFO 级别可见");
        logger.warn("默认 profile 下，WARN 级别可见");

        // 验证 Logger 实现类是 Logback（Spring Boot 默认）
        Logger rootLogger = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        System.out.println("Root Logger 实现类: " + rootLogger.getClass().getName());
        System.out.println("当前 Logger 实现类: " + logger.getClass().getName());
    }
}
