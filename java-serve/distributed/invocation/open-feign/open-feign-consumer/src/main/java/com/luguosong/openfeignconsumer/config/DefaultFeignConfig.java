package com.luguosong.openfeignconsumer.config;

import feign.Logger;
import org.springframework.context.annotation.Bean;

/**
 * @author luguosong
 */
public class DefaultFeignConfig {

	@Bean
	public Logger.Level level() {
		return Logger.Level.FULL;
	}
}
