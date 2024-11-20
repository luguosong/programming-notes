package com.upda.gatewaycustomgatewayfilter.filter;

import lombok.Data;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * PrintAnyGatewayFilterFactory类名，其中
 * 前半段PrintAny：对应配置文件中过滤器的键值
 * 后半段GatewayFilterFactory：是固定写法，方便配置使用
 *
 * @author luguosong
 */
@Component
public class PrintAnyGatewayFilterFactory extends AbstractGatewayFilterFactory<PrintAnyGatewayFilterFactory.Config> {

	@Override
	public GatewayFilter apply(Config config) {
		/*
		* 参数一：路由过滤器
		* 参数二：过滤器执行顺序
		* */
		return new OrderedGatewayFilter(
				new GatewayFilter() {
					@Override
					public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

						System.out.println("自定义路由过滤器执行");
						System.out.println("参数一：" + config.getA());
						System.out.println("参数二：" + config.getB());
						System.out.println("参数三：" + config.getC());

						return chain.filter(exchange);
					}
				}
				, 0);
	}


	@Data
	public static class Config {
		private String a;
		private String b;
		private String c;
	}

	// 返回Config类中的字段名称，参数将按此顺序获取
	@Override
	public List<String> shortcutFieldOrder() {
		return List.of("a", "b", "c");
	}

	public PrintAnyGatewayFilterFactory() {
		super(Config.class);
	}
}
