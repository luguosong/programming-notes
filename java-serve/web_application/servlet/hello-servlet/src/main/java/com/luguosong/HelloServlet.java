package com.luguosong;

import jakarta.servlet.*;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author luguosong
 */
public class HelloServlet implements Servlet {
    @Override
    public void init(ServletConfig servletConfig) throws ServletException {

    }

    @Override
    public ServletConfig getServletConfig() {
        return null;
    }


    /**
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        System.out.println("业务处理...");

        // 设置响应内容类型
        response.setContentType("text/html;charset=utf-8");
        // 将响应的数据写入到输出流中
        PrintWriter out = response.getWriter();
        out.println("<h1>hello world</h1>");
    }

    @Override
    public String getServletInfo() {
        return "";
    }

    @Override
    public void destroy() {

    }
}
