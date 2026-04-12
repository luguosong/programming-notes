package com.luguosong.jul;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.logging.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Logger 父子关系演示
 */
class JulParentChildTest {

    private final java.util.List<Logger> cleanupList = new java.util.ArrayList<>();

    @AfterEach
    void cleanup() {
        // 测试结束后重置 LogManager，避免测试间相互影响
        LogManager.getLogManager().reset();
    }

    /**
     * 通过包名层级创建父子 Logger，验证日志传递行为
     */
    @Test
    void testParentChildRelationship() {
        // 创建父 Logger（短包名）
        Logger parentLogger = Logger.getLogger("com.luguosong");
        parentLogger.setUseParentHandlers(false);
        parentLogger.setLevel(Level.ALL);

        // 为父 Logger 添加 Handler，用于捕获日志
        StringBuilder parentBuffer = new StringBuilder();
        parentLogger.addHandler(new Handler() {
            @Override
            public void publish(LogRecord record) {
                parentBuffer.append("[").append(record.getLevel()).append("] ")
                        .append(record.getMessage()).append("\n");
            }

            @Override
            public void flush() {}

            @Override
            public void close() {}
        });

        // 创建子 Logger（长包名，是父 Logger 的子级）
        Logger childLogger = Logger.getLogger("com.luguosong.jul");
        childLogger.setLevel(Level.ALL);

        // 子 Logger 输出日志
        childLogger.info("子 Logger 发出的日志");

        // 验证：子 Logger 的日志默认会传递给父 Logger 的 Handler
        String parentOutput = parentBuffer.toString();
        assertTrue(parentOutput.contains("子 Logger 发出的日志"),
                "子 Logger 的日志应传递给父 Logger 的 Handler");
        System.out.println("父 Logger 收到的日志:\n" + parentOutput);
    }

    /**
     * setUseParentHandlers(false) 阻止日志传递给父 Handler
     */
    @Test
    void testDisableParentHandler() {
        // 创建父 Logger
        Logger parentLogger = Logger.getLogger("com.luguosong");
        parentLogger.setUseParentHandlers(false);
        parentLogger.setLevel(Level.ALL);

        // 为父 Logger 添加 Handler
        StringBuilder parentBuffer = new StringBuilder();
        parentLogger.addHandler(new Handler() {
            @Override
            public void publish(LogRecord record) {
                parentBuffer.append("[").append(record.getLevel()).append("] ")
                        .append(record.getMessage()).append("\n");
            }

            @Override
            public void flush() {}

            @Override
            public void close() {}
        });

        // 创建子 Logger 并设置 setUseParentHandlers(false)
        Logger childLogger = Logger.getLogger("com.luguosong.jul");
        childLogger.setLevel(Level.ALL);
        childLogger.setUseParentHandlers(false); // 关键：阻止传递给父 Handler

        // 为子 Logger 添加自己的 Handler
        StringBuilder childBuffer = new StringBuilder();
        childLogger.addHandler(new Handler() {
            @Override
            public void publish(LogRecord record) {
                childBuffer.append("[").append(record.getLevel()).append("] ")
                        .append(record.getMessage()).append("\n");
            }

            @Override
            public void flush() {}

            @Override
            public void close() {}
        });

        // 子 Logger 输出日志
        childLogger.info("这条日志不会传递给父 Logger");

        // 验证：子 Logger 的 Handler 收到了日志
        assertTrue(childBuffer.toString().contains("这条日志不会传递给父 Logger"),
                "子 Logger 自己的 Handler 应收到日志");

        // 验证：父 Logger 的 Handler 没有收到日志
        assertFalse(parentBuffer.toString().contains("这条日志不会传递给父 Logger"),
                "父 Logger 的 Handler 不应收到子 Logger 的日志");

        System.out.println("子 Logger 日志:\n" + childBuffer);
        System.out.println("父 Logger 日志（应为空）:\n" + parentBuffer);
    }
}
