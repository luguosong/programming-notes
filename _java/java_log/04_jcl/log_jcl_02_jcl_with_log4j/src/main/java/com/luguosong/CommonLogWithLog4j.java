package com.luguosong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.BasicConfigurator;

/**
 * 一旦引入log4j依赖，JCL将不再默认使用JUL，而是使用log4j
 *
 * @author luguosong
 */
public class CommonLogWithLog4j {
    public static void main(String[] args) {
        //初始化配置
        //为RootLogger添加ConsoleAppender
        //在没有配置文件的情况下需要执行这条语句
        BasicConfigurator.configure();

        // 1.创建日志记录器对象
        Log log = LogFactory.getLog(CommonLogWithLog4j.class);

        // 2.日志记录输出
        log.info("info信息");
    }
}
