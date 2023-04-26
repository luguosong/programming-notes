package com.luguosong.child;

import org.apache.log4j.Logger;
import org.apache.log4j.helpers.LogLog;

/**
 * 对应配置文件中配置的子日志记录器
 * log4j.logger.com.luguosong.child=error, toConsole
 *
 * @author luguosong
 */
public class ChildLogger {
    public static void main(String[] args) {
        LogLog.setInternalDebugging(true);

        Logger logger = Logger.getLogger(ChildLogger.class);

        logger.trace("trace信息");
        logger.debug("debug信息");
        logger.info("info信息");
        logger.warn("warn信息");
        logger.error("error信息");
        logger.fatal("fatal信息");
    }
}
