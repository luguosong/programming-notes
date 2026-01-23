package com.luguosong.ssiach6ex4.config;


import com.luguosong.ssiach6ex4.handlers.CustomAuthenticationFailureHandler;
import com.luguosong.ssiach6ex4.handlers.CustomAuthenticationSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class ProjectConfig {

	private final CustomAuthenticationSuccessHandler authenticationSuccessHandler;
	private final CustomAuthenticationFailureHandler authenticationFailureHandler;

	public ProjectConfig(CustomAuthenticationSuccessHandler authenticationSuccessHandler,
						 CustomAuthenticationFailureHandler authenticationFailureHandler) {
		this.authenticationSuccessHandler = authenticationSuccessHandler;
		this.authenticationFailureHandler = authenticationFailureHandler;
	}

	@Bean
	public UserDetailsService uds() {
		var uds = new InMemoryUserDetailsManager();

		uds.createUser(
				User.withDefaultPasswordEncoder()
						.username("john")
						.password("12345")
						.authorities("read")
						.build()
		);

		uds.createUser(
				User.withDefaultPasswordEncoder()
						.username("bill")
						.password("12345")
						.authorities("write")
						.build()
		);

		return uds;
	}


	@Bean
	public SecurityFilterChain configure(HttpSecurity http) throws Exception {
		http.formLogin(c ->
				c.successHandler(authenticationSuccessHandler)
						.failureHandler(authenticationFailureHandler)
		);

		http.httpBasic(Customizer.withDefaults());

		http.authorizeHttpRequests(c -> c.anyRequest().authenticated());

		return http.build();
	}
}
