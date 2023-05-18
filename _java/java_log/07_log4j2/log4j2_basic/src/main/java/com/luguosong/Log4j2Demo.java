package com.luguosong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author luguosong
 */
public class Log4j2Demo {
    public static void main(String[] args) {
        // 获取日志记录器对象
        Logger logger = LoggerFactory.getLogger(Log4j2Demo.class);

        // 日志记录输出
        logger.error("error错误信息");
        logger.warn("warn警告信息");
        logger.info("info关键信息");
        logger.debug("debug详细信息");
        logger.trace("trace追踪信息");

    }
}
