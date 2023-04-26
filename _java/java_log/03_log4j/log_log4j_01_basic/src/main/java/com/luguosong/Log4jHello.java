package com.luguosong;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

/**
 * @author luguosong
 */
public class Log4jHello {
    public static void main(String[] args) {

        //初始化配置
        //为RootLogger添加ConsoleAppender
        //在没有配置文件的情况下需要执行这条语句
        BasicConfigurator.configure();

        Logger logger = Logger.getLogger(Log4jHello.class);

        logger.trace("trace信息");
        logger.debug("debug信息");
        logger.info("info信息");
        logger.warn("warn信息");
        logger.error("error信息");
        logger.fatal("fatal信息");
    }
}
