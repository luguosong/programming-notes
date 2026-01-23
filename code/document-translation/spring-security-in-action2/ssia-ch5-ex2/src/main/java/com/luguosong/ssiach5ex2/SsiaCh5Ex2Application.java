package com.luguosong.ssiach5ex2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
public class SsiaCh5Ex2Application {

	public static void main(String[] args) {
		SpringApplication.run(SsiaCh5Ex2Application.class, args);
	}

}
