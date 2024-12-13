package com.luguosong.openfeignconsumer;

import com.luguosong.openfeignconsumer.config.DefaultFeignConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(defaultConfiguration = DefaultFeignConfig.class)
@SpringBootApplication
public class OpenFeignConsumerApplication {

	public static void main(String[] args) {
		SpringApplication.run(OpenFeignConsumerApplication.class, args);
	}

}
