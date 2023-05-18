package com.luguosong.log_with_springboot;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class LogWithSpringbootApplicationTests {

	@Test
	public void contextLoads() {
		Logger logger = LoggerFactory.getLogger(LogWithSpringbootApplicationTests.class);

		// 日志记录输出
		logger.error("error错误信息");
		logger.warn("warn警告信息");
		logger.info("info关键信息");
		logger.debug("debug详细信息");
		logger.trace("trace追踪信息");
	}

}
