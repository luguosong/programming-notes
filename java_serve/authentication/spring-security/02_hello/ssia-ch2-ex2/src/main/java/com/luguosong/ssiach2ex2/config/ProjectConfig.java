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

/**
 * @author luguosong
 */
@Configuration
public class ProjectConfig {

    @Bean
    SecurityFilterChain configure(HttpSecurity http)
            throws Exception {
        //配置认证方式为Http Basic
        http.httpBasic(Customizer.withDefaults());

        // 请求需要进行认证（默认方式）
        http.authorizeHttpRequests(
                c -> c.anyRequest().authenticated()
        );

        return http.build();
    }


    @Bean
    UserDetailsService userDetailsService() {
        var user = User.withUsername("john")
                .password("12345")
                .authorities("read")
                .build();

        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

}
