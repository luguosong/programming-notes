import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author luguosong
 */
public class Slf4jWithLog4j {
    public static void main(String[] args) {
        //log4j使用默认的配置信息，不需要写log4j.properties
        BasicConfigurator.configure();

        Logger logger = LoggerFactory.getLogger(Slf4jWithLog4j.class);

        logger.error("error错误信息");
        logger.warn("warn警告信息");
        logger.info("info关键信息");
        logger.debug("debug详细信息");
        logger.trace("trace追踪信息");
    }
}
