package com.luguosong.ssiach8ex5.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;

@Configuration
public class ProjectConfig {

	@Bean
	public UserDetailsService userDetailsService() {
		var uds = new InMemoryUserDetailsManager();

		var u1 = User.withUsername("john")
				.password("12345")
				.authorities("read")
				.build();

		var u2 = User.withUsername("jane")
				.password("12345")
				.authorities("read", "premium")
				.build();

		uds.createUser(u1);
		uds.createUser(u2);

		return uds;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return NoOpPasswordEncoder.getInstance();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.httpBasic(Customizer.withDefaults());

		http.authorizeHttpRequests(
				c ->
						c.requestMatchers(new RegexRequestMatcher(".*/[us|uk|ca]+/[en|fr].*", HttpMethod.GET.name())).authenticated()
								.anyRequest().hasAuthority("premium")
		);

		return http.build();
	}
}
