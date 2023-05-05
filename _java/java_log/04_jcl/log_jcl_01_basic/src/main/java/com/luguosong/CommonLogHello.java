package com.luguosong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 默认情况下，会使用JUL框架
 *
 * @author luguosong
 */
public class CommonLogHello {
    public static void main(String[] args) {
        // 1.创建日志记录器对象
        Log log = LogFactory.getLog(CommonLogHello.class);

        // 2.日志记录输出
        log.info("info信息");
    }
}
