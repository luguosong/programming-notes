package com.luguosong;

import ch.qos.logback.classic.LoggerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author luguosong
 */
public class LogBackDemo {
    public static void main(String[] args) {
        Logger logger = LoggerFactory.getLogger(LogBackDemo.class);


        for (int i = 0; i < 100; i++) {
            logger.error("error错误信息");
            logger.warn("warn警告信息");
            logger.info("info关键信息");
            logger.debug("debug详细信息");
            logger.trace("trace追踪信息");
        }

        System.out.println("1111111111");
        System.out.println("2222222222");
        System.out.println("3333333333");
        System.out.println("4444444444");
        System.out.println("5555555555");

        //防止异步日志未写完控制台就关闭了
        LoggerContext factory = (LoggerContext) LoggerFactory.getILoggerFactory();
        factory.stop();
    }
}
