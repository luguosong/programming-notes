package com.luguosong.ssiach5ex1.filters;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * 自定义过滤器
 *
 * @author luguosong
 */
public class RequestValidationFilter implements Filter {
	@Override
	public void doFilter(ServletRequest request,
						 ServletResponse response,
						 FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		String requestId = httpRequest.getHeader("Request-Id");
		// 如果请求中不存在Request-Id头，应用程序将设置HTTP状态为400 Bad Request并返回给客户端
		if (requestId == null || requestId.isBlank()) {
			httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		// 执行下一个过滤器
		filterChain.doFilter(request, response);
	}
}
