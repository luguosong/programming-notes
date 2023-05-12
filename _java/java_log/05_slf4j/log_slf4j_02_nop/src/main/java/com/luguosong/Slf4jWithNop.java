package com.luguosong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author luguosong
 */
public class Slf4jWithNop {
    public static void main(String[] args) {
        Logger logger = LoggerFactory.getLogger(Slf4jWithNop.class);

        logger.error("error错误信息");
        logger.warn("warn警告信息");
        logger.info("info关键信息");
        logger.debug("debug详细信息");
        logger.trace("trace追踪信息");
    }
}
