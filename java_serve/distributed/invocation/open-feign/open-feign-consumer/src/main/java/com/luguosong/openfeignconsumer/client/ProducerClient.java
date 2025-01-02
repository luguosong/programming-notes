package com.luguosong.openfeignconsumer.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author luguosong
 */
@FeignClient(value = "open-feign-producer")
public interface ProducerClient {

	@GetMapping("/demo")
	String demo();
}
