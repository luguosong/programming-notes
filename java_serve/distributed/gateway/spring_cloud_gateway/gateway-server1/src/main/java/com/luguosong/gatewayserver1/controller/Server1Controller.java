package com.luguosong.gatewayserver1.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author luguosong
 */
@RestController
public class Server1Controller {
	@GetMapping("/demo")
	public String demo() {
		return "服务gateway-server1被调用";
	}
}
