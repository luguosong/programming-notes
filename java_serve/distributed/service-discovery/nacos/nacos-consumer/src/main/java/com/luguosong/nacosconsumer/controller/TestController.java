package com.luguosong.nacosconsumer.controller;

import cn.hutool.core.util.RandomUtil;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * @author luguosong
 */
@RestController
@RequiredArgsConstructor
public class TestController {

	//通过构造方法进行依赖注入
	private final DiscoveryClient discoveryClient;

	@GetMapping("/hello")
	public String helloNacos() throws NacosException {
		// 根据名字获取nacos中的实例列表
		List<ServiceInstance> instances = discoveryClient.getInstances("nacos-producer");
		// 随机获取一个实例
		ServiceInstance instance = instances.get(RandomUtil.randomInt(instances.size()));

		// 通过RestTemplate发送请求
		ResponseEntity<String> response = new RestTemplate().exchange(instance.getUri() + "/demo",
				HttpMethod.GET,
				null,
				String.class);

		return response.getBody();
	}
}
