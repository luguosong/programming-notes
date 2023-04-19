package com.luguosong;

import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * 将日志输出到文件中
 *
 * @author luguosong
 */
public class HandlerToFile {
    public static void main(String[] args) {
        try {
            //获取日志记录器
            Logger logger = Logger.getLogger("com.luguosong.HandlerToFile");


            //不按父logger默认方式进行操作
            logger.setUseParentHandlers(false);


            //在文件中输出日志
            FileHandler handler1 = new FileHandler("./_java/java_log/02_jul/log_jul_01_basic/src/main/resources/HandlerToFile.log");
            handler1.setFormatter(new SimpleFormatter());
            logger.addHandler(handler1);


            //同时在控制台输出日志
            ConsoleHandler handler2 = new ConsoleHandler();
            handler2.setFormatter(new SimpleFormatter());
            logger.addHandler(handler2);


            //打印日志
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
