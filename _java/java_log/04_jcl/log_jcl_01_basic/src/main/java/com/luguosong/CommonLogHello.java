package com.luguosong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

/**
 * 默认情况下，会使用JUL框架
 *
 * @author luguosong
 */
public class CommonLogHello {
    public static void main(String[] args) throws IOException {



        // 1.创建日志记录器对象
        Log log = LogFactory.getLog(CommonLogHello.class);

        // 2.日志记录输出
        log.trace("This is a TRACE message");
        log.debug("This is a DEBUG message");
        log.info("This is an INFO message");
        log.warn("This is a WARN message");
        log.error("This is an ERROR message");
        log.fatal("This is a FATAL message");

    }
}
