package com.luguosong.ioc.annotation.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 通过@ComponentScan注解配置包扫描
 *
 * @author luguosong
 */
@Configuration
@ComponentScan({"com.luguosong.ioc.annotation.config"})
public class SpringConfig {
}
