package com.luguosong.openfeignconsumer.controller;

import com.luguosong.openfeignconsumer.client.ProducerClient;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author luguosong
 */
@RestController
@RequiredArgsConstructor
public class TestController {

	private final ProducerClient client;

	@GetMapping("/test")
	public String test() {
		return client.demo();
	}
}
