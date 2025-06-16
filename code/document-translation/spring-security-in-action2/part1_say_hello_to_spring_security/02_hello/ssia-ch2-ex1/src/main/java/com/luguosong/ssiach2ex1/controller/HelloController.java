package com.luguosong.ssiach2ex1.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author luguosong
 */
@RestController
public class HelloController {

	@GetMapping("/hello")
	public String hello() {
		return "Hello!";
	}
}
