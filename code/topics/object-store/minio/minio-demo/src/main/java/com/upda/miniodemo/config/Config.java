package com.upda.miniodemo.config;

import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author luguosong
 */
@Configuration
public class Config {

	@Bean
	public MinioClient minioClient() {
		return MinioClient.builder()
				.endpoint("http://127.0.0.1:9000")
				.credentials("minioadmin", "minioadmin")
				.build();
	}
}
