package com.luguosong;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

/**
 * @author luguosong
 */
public class Log4jToLogback {
    public static void main(String[] args) {
        BasicConfigurator.configure();

        Logger logger = Logger.getLogger(Log4jToLogback.class);

        logger.trace("trace信息");
        logger.debug("debug信息");
        logger.info("info信息");
        logger.warn("warn信息");
        logger.error("error信息");
        logger.fatal("fatal信息");
    }
}
