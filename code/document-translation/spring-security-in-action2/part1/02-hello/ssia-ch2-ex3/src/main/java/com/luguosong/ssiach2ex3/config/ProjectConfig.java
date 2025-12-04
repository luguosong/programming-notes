package com.luguosong.ssiach2ex3.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class ProjectConfig {

  @Bean
  SecurityFilterChain configure(HttpSecurity http) throws Exception {
	//  配置Http Basic认证
    http.httpBasic(Customizer.withDefaults());

	// 鉴权
    http.authorizeHttpRequests(
        c -> c.anyRequest().authenticated()
    );

    var user = User.withUsername("john")
        .password("12345")
        .authorities("read")
        .build();

    var userDetailsService = new InMemoryUserDetailsManager(user);

	// 配置UserDetailsService
    http.userDetailsService(userDetailsService);

    return http.build();
  }

  // 密码编码器
  @Bean
  PasswordEncoder passwordEncoder() {
    return NoOpPasswordEncoder.getInstance();
  }
}
