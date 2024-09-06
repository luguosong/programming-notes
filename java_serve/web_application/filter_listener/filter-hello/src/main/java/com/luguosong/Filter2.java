package com.luguosong;

import jakarta.servlet.*;

import java.io.IOException;

/**
 * @author luguosong
 */
public class Filter2 implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        System.out.println("过滤器2请求执行");
        chain.doFilter(request, response);
        System.out.println("过滤器2响应执行");
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
