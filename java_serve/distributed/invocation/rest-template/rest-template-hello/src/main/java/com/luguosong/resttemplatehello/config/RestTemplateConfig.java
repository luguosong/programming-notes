package com.luguosong.resttemplatehello.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * @author luguosong
 */
@Configuration
public class RestTemplateConfig {

	/*
	* 将RestTemplate注册到Spring容器
	* */
	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
}
