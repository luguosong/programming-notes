package com.upda.gatewaycustomglobalfilter.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author luguosong
 */
@Component
public class MyGlobalFilter implements GlobalFilter, Ordered {

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		// 获取request可以进行参数判断
		ServerHttpRequest request = exchange.getRequest();

		System.out.println("全局过滤器执行了");

		return chain.filter(exchange);
	}


	@Override
	public int getOrder() {
		//过滤器执行顺序，值越小越优先
		return 0;
	}
}
