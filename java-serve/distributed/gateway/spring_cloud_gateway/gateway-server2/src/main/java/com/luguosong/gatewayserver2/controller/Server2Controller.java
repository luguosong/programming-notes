package com.luguosong.gatewayserver2.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author luguosong
 */
@RestController
public class Server2Controller {
	@GetMapping("/demo")
	public String demo() {
		return "服务gateway-server2被调用";
	}
}
