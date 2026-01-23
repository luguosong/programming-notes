package com.luguosong.ssiach2ex2.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class ProjectConfig {

	@Bean
	SecurityFilterChain configure(HttpSecurity http) throws Exception {
		// 指定应用程序采用 HTTP Basic 作为认证方式
		http.httpBasic(Customizer.withDefaults());

		// 指定支持基于表单的认证。如果未指定 FormLoginConfigurer.loginPage(String)，则会生成一个默认的登录页面。
		http.formLogin(Customizer.withDefaults());

		// 用于在端点级别配置授权规则。
		// 通过调用该方法，你可以指定应用程序如何对特定端点接收到的请求进行授权。
		// 通过该配置，可以让请求跳过认证
		http.authorizeHttpRequests(
				// 已认证的情况下允许访问
				c -> c.anyRequest().authenticated()

				// 无需凭证的情况下允许访问
				//c -> c.anyRequest().permitAll()
		);

		return http.build();
	}

	// 自定义UserDetailsService Bean
	@Bean
	UserDetailsService userDetailsService() {
		var user = User.withUsername("john")
				.password("12345")
				.authorities("read")
				.build();

		return new InMemoryUserDetailsManager(user);
	}

	// 如果自定义UserDetailsService,则需要提供一个 PasswordEncoder Bean。
	@Bean
	PasswordEncoder passwordEncoder() {
		return NoOpPasswordEncoder.getInstance();
	}
}
