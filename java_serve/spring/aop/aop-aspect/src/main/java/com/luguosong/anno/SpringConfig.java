package com.luguosong.anno;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * 注解@EnableAspectJAutoProxy表示开启自动代理
 * @author luguosong
 */
@Configuration
@ComponentScan({"com.luguosong.anno"})
@EnableAspectJAutoProxy
public class SpringConfig {
}
