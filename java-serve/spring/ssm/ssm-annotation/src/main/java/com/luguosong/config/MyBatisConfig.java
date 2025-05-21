package com.luguosong.config;

import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

/**
 * @author luguosong
 */
public class MyBatisConfig {

	@Bean
	public SqlSessionFactoryBean getSqlSessionFactoryBean(DataSource dataSource) {
		SqlSessionFactoryBean sessionFactoryBean = new SqlSessionFactoryBean();
		sessionFactoryBean.setDataSource(dataSource);
		sessionFactoryBean.setTypeAliasesPackage("com.luguosong.bean");
		return sessionFactoryBean;
	}

	@Bean
	public MapperScannerConfigurer getMapperScannerConfigurer() {
		MapperScannerConfigurer msc = new MapperScannerConfigurer();
		msc.setBasePackage("com.luguosong.dao");
		return msc;
	}
}
