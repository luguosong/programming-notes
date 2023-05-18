package com.luguosong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author luguosong
 */
public class AsyncLoggerDemo {
    public static void main(String[] args) {
        // 获取日志记录器对象
        Logger logger = LoggerFactory.getLogger(AsyncLoggerDemo.class);

        // 模拟日志记录输出
        for (int i = 0; i < 100; i++) {
            logger.error("error错误信息");
            logger.warn("warn警告信息");
            logger.info("info关键信息");
            logger.debug("debug详细信息");
            logger.trace("trace追踪信息");
        }

        //模拟程序执行
        for (int i = 0; i < 1000; i++) {
            System.out.println("------------------");
        }
    }
}
