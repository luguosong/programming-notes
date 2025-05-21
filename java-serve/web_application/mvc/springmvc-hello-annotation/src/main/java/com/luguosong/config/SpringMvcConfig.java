package com.luguosong.config;

import com.luguosong.interceptors.MyInterceptor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ITemplateResolver;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

/**
 * 注解@ComponentScan配置包扫描，相当于<context:component-scan/>标签
 * 注解@EnableWebMvc开启Spring MVC注解驱动，相当于<mvc:annotation-driven/>标签
 *
 * @author luguosong
 */
@Configuration
@ComponentScan("com.luguosong.controller")
@EnableWebMvc
public class SpringMvcConfig implements WebMvcConfigurer {
	/*
	 * 配置Thymeleaf视图解析器
	 * */
	@Bean
	public ThymeleafViewResolver getThymeleafViewResolver(SpringTemplateEngine springTemplateEngine) {
		ThymeleafViewResolver resolver = new ThymeleafViewResolver();
		resolver.setTemplateEngine(springTemplateEngine);
		resolver.setCharacterEncoding("UTF-8");
		resolver.setOrder(1);
		return resolver;
	}

	@Bean
	public SpringTemplateEngine getSpringTemplateEngine(ITemplateResolver iTemplateResolver) {
		SpringTemplateEngine springTemplateEngine = new SpringTemplateEngine();
		springTemplateEngine.setTemplateResolver(iTemplateResolver);
		return springTemplateEngine;
	}

	@Bean
	public ITemplateResolver getITemplateResolver(ApplicationContext applicationContext) {
		SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
		resolver.setApplicationContext(applicationContext);
		resolver.setPrefix("/WEB-INF/templates/");
		resolver.setSuffix(".html");
		resolver.setTemplateMode(TemplateMode.HTML);
		resolver.setCharacterEncoding("UTF-8");
		resolver.setCacheable(false); //开发环境关闭缓存，生产环境建议开启缓存
		return resolver;
	}

	/*
	 * 开启静态资源访问
	 * */
	@Override
	public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
		configurer.enable();
	}

	/*
	 * 配置试图控制器
	 * */
	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/view-controller").setViewName("view-controller");
	}

	/*
	 * 配置异常处理器
	 * */
	@Override
	public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
		SimpleMappingExceptionResolver resolver = new SimpleMappingExceptionResolver();
		Properties properties = new Properties();
		properties.setProperty("java.lang.Exception", "error");
		resolver.setExceptionMappings(properties);
		resolver.setExceptionAttribute("errMsg");
		resolvers.add(resolver);
	}

	/*
	 * 配置拦截器
	 * */

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		MyInterceptor myInterceptor = new MyInterceptor();

		registry.addInterceptor(myInterceptor)
				.addPathPatterns("/**")
				.excludePathPatterns("/error");
	}
}
