package com.luguosong.ssiach5ex1.filters;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class RequestValidationFilter implements Filter {

	@Override
	public void doFilter(ServletRequest request,
						 ServletResponse response,
						 FilterChain filterChain) throws IOException, ServletException {
		var httpRequest = (HttpServletRequest) request;
		var httpResponse = (HttpServletResponse) response;

		// 获取Request-Id头
		String requestId = httpRequest.getHeader("Request-Id");

		// 如果Request-Id头缺失或为空，则返回400错误
		if (requestId == null || requestId.isBlank()) {
			httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		// 继续处理请求
		filterChain.doFilter(request, response);
	}
}
