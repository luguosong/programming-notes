package com.luguosong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * slf4j集成logback
 *
 * @author luguosong
 */
public class slf4jWithLogback {
    public static void main(String[] args) {
        Logger logger = LoggerFactory.getLogger(slf4jWithLogback.class);

        logger.error("error错误信息");
        logger.warn("warn警告信息");
        logger.info("info关键信息");
        logger.debug("debug详细信息");
        logger.trace("trace追踪信息");
    }
}
