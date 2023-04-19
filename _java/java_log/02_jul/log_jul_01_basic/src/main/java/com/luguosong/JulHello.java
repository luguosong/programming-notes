package com.luguosong;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * JUL日志测试
 *
 * @author luguosong
 */
public class JulHello {
    public static void main(String[] args) {
        //获取日志记录器
        Logger logger = Logger.getLogger("com.luguosong.JulTest");


        //设置日志记录器的日志级别
        logger.setUseParentHandlers(false); //不按父logger默认方式进行操作
        ConsoleHandler handler = new ConsoleHandler(); //获取控制台日志处理器
        SimpleFormatter formatter = new SimpleFormatter(); //创建日志格式化组件对象
        handler.setFormatter(formatter); //设置日志格式化组件
        logger.addHandler(handler); //添加日志处理器
        //设置日志级别,显示大于该级别的日志信息
        //默认为Level.INFO
        //注意：需要同时设置Logger和Handler的级别，输出才会生效
        logger.setLevel(Level.ALL);
        handler.setLevel(Level.ALL);


        //方式一
        logger.severe("我是severe消息"); //错误级别,级别整数值1000
        logger.warning("我是warning消息"); //警告级别,级别整数值900
        logger.info("我是info消息"); //消息级别,级别整数值800
        logger.config("我是config消息"); //配置级别,级别整数值700
        logger.fine("我是fine消息"); //细节级别,级别整数值500
        logger.finer("我是finer消息"); //更细节级别,级别整数值400
        logger.finest("我是finest消息"); //最细节级别，级别整数值300


        //方式二
        logger.log(Level.SEVERE, "我是severe消息"); //错误级别
        logger.log(Level.WARNING, "我是warning消息"); //警告级别
        logger.log(Level.INFO, "我是info消息"); //消息级别
        logger.log(Level.CONFIG, "我是config消息"); //配置级别
        logger.log(Level.FINE, "我是fine消息"); //细节级别
        logger.log(Level.FINER, "我是finer消息"); //更细节级别
        logger.log(Level.FINEST, "我是finest消息"); //最细节级别


        //动态日志
        logger.log(Level.INFO, "姓名：{0}，年龄：{1}", new Object[]{"张三", 18});
    }
}
