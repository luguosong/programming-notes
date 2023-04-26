package com.luguosong;

import org.apache.log4j.Logger;
import org.apache.log4j.helpers.LogLog;

/**
 * @author luguosong
 */
public class Log4jDebugging {
    public static void main(String[] args) {

        //打印log4j的debug信息
        LogLog.setInternalDebugging(true);

        Logger logger = Logger.getLogger(Log4jHello.class);

        logger.trace("trace信息");
        logger.debug("debug信息");
        logger.info("info信息");
        logger.warn("warn信息");
        logger.error("error信息");
        logger.fatal("fatal信息");
    }
}
