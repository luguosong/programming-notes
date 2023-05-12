import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基于slf4j-simple的日志输出
 *
 * @author luguosong
 */
public class HelloSlf4j {
    public static void main(String[] args) {
        Logger logger = LoggerFactory.getLogger(HelloSlf4j.class);

        logger.error("error错误信息");
        logger.warn("warn警告信息");
        logger.info("info关键信息");
        logger.debug("debug详细信息");
        logger.trace("trace追踪信息");

        // 通过占位符{}输出变量
        logger.info("学生姓名{}，年龄{}","张三",18);

        //异常信息打印
        try {
            Class.forName("aaa");
        } catch (ClassNotFoundException e) {
            logger.info("出现ClassNotFoundException异常");
            logger.info("具体错误是：",e);
        }
    }
}
