package com.luguosong.config;

import jakarta.servlet.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

/**
 * 该类相当于web.xml
 *
 * @author luguosong
 */
@Configuration
public class WebAppInitialize extends AbstractAnnotationConfigDispatcherServletInitializer {
	/*
	 * Spring配置
	 * */
	@Override
	protected Class<?>[] getRootConfigClasses() {
		return new Class[]{SpringConfig.class};
	}

	/*
	 * Spring MVC配置
	 * */
	@Override
	protected Class<?>[] getServletConfigClasses() {
		// 指定Spring MVC配置类
		return new Class[]{SpringMvcConfig.class};
	}

	/*
	 * 配置DispatcherServlet的url-pattern
	 * */
	@Override
	protected String[] getServletMappings() {
		return new String[]{"/"};
	}

	/*
	 * 配置过滤器
	 * */
	@Override
	protected Filter[] getServletFilters() {

		// 配置字符编码过滤器
		CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
		characterEncodingFilter.setEncoding("UTF-8");
		characterEncodingFilter.setForceRequestEncoding(true);
		characterEncodingFilter.setForceResponseEncoding(true);
		//配置过滤器，让form表单支持"PUT"、"DELETE"和"PATCH"方法
		HiddenHttpMethodFilter hiddenHttpMethodFilter = new HiddenHttpMethodFilter();

		return new Filter[]{characterEncodingFilter, hiddenHttpMethodFilter};
	}
}
