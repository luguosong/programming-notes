package com.luguosong.ssiach5ex3.filters;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.logging.Logger;

public class AuthenticationLoggingFilter extends OncePerRequestFilter {

	private final Logger logger =
			Logger.getLogger(AuthenticationLoggingFilter.class.getName());


	@Override
	protected void doFilterInternal(HttpServletRequest request,
									HttpServletResponse response,
									FilterChain filterChain) throws ServletException, IOException {

		String requestId = request.getHeader("Request-Id");

		logger.info("已成功验证请求 ID" + requestId);

		filterChain.doFilter(request, response);
	}
}
