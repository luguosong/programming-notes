package com.lugusong;

import java.io.FileInputStream;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * 自定义jul配置文件
 *
 * @author luguosong
 */
public class CustomConfiguration {
    public static void main(String[] args) {
        try {
            //读取自定义配置文件
            LogManager logManager = LogManager.getLogManager();
            logManager.readConfiguration(new FileInputStream("_java/java_log/02_jul/log_jul_02_custom_configuration_file/src/main/resources/log.properties"));

            Logger logger = Logger.getLogger("com.lugusong.CustomConfiguration");

            logger.severe("我是severe消息"); //错误级别,级别整数值1000
            logger.warning("我是warning消息"); //警告级别,级别整数值900
            logger.info("我是info消息"); //消息级别,级别整数值800
            logger.config("我是config消息"); //配置级别,级别整数值700
            logger.fine("我是fine消息"); //细节级别,级别整数值500
            logger.finer("我是finer消息"); //更细节级别,级别整数值400
            logger.finest("我是finest消息"); //最细节级别，级别整数值300

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
