package com.luguosong;

import org.apache.log4j.Logger;
import org.apache.log4j.helpers.LogLog;

/**
 * @author luguosong
 */
public class Log4jConfig {
    public static void main(String[] args) {
        LogLog.setInternalDebugging(true);

        Logger logger = Logger.getLogger(Log4jConfig.class);

        for (int i = 0; i < 100; i++) {
            logger.trace("trace信息" + i);
            logger.debug("debug信息" + i);
            logger.info("info信息" + i);
            logger.warn("warn信息" + i);
            logger.error("error信息" + i);
            logger.fatal("fatal信息" + i);
        }

        int a = 1 / 0;

        1+1
    }
}
