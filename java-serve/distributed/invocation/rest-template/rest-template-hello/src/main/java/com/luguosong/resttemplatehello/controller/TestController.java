package com.luguosong.resttemplatehello.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * @author luguosong
 */
@RestController
@RequiredArgsConstructor
public class TestController {

	//使用构造函数依赖注入RestTemplate
	private final RestTemplate template;

	@GetMapping("/test")
	public String test() {
		//获取响应
		ResponseEntity<String> response = template.exchange("http://localhost:8080/demo",
				HttpMethod.GET,
				null,
				String.class);

		//判断响应是否成功
		if (!response.getStatusCode().is2xxSuccessful()) {
			return "服务调用失败";
		}

		//获取并返回响应体
		return response.getBody();
	}
}
