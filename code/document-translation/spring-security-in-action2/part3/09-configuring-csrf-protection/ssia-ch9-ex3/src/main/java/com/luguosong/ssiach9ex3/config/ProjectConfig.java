package com.luguosong.ssiach9ex3.config;


import com.luguosong.ssiach9ex3.csrf.CustomCsrfTokenRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
public class ProjectConfig {

	private final CustomCsrfTokenRepository customTokenRepository;

	public ProjectConfig(CustomCsrfTokenRepository customTokenRepository) {
		this.customTokenRepository = customTokenRepository;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(c -> {
			c.csrfTokenRepository(customTokenRepository);
			c.csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler());
//            c.ignoringRequestMatchers("/ciao");

//            HandlerMappingIntrospector i = new HandlerMappingIntrospector();
//            MvcRequestMatcher r = new MvcRequestMatcher(i, "/ciao");
//            c.ignoringRequestMatchers(r);

//            String pattern = ".*[0-9].*";
//            String httpMethod = HttpMethod.POST.name();
//            RegexRequestMatcher r = new RegexRequestMatcher(pattern, httpMethod);
//            c.ignoringRequestMatchers(r);
		});

		http.authorizeHttpRequests(
				c -> c.anyRequest().permitAll()
		);

		return http.build();
	}
}
