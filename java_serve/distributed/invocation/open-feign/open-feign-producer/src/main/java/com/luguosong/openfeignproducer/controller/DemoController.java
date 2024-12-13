package com.luguosong.openfeignproducer.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author luguosong
 */
@RestController
public class DemoController {
	@GetMapping("/demo")
	public String demo() {
		return "nacos：服务调用成功";
	}
}
