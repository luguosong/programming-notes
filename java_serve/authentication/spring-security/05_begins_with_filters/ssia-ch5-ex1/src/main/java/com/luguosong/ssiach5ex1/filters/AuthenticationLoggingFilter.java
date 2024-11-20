package com.luguosong.ssiach5ex1.filters;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * @author luguosong
 */
public class AuthenticationLoggingFilter implements Filter {

	private final Logger logger =
			Logger.getLogger(AuthenticationLoggingFilter.class.getName());

	@Override
	public void doFilter(ServletRequest request,
						 ServletResponse response,
						 FilterChain filterChain) throws IOException, ServletException {
		var httpRequest = (HttpServletRequest) request;
		String requestId = httpRequest.getHeader("Request-Id");
		logger.info("请求已成功通过身份验证，ID为 " +  requestId);
		filterChain.doFilter(request, response);
	}
}
