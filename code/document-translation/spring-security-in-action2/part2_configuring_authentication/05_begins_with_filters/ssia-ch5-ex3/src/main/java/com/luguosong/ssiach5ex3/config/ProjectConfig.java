package com.luguosong.ssiach5ex3.config;

import com.luguosong.ssiach5ex3.filters.AuthenticationLoggingFilter;
import com.luguosong.ssiach5ex3.filters.RequestValidationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
public class ProjectConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.addFilterBefore(
                new RequestValidationFilter(), BasicAuthenticationFilter.class)
            .addFilterAfter(
                new AuthenticationLoggingFilter(), BasicAuthenticationFilter.class)
            .authorizeHttpRequests(c -> c.anyRequest().permitAll());

        return http.build();
    }
}
