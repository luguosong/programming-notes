package com.luguosong;

import jakarta.servlet.*;

import java.io.IOException;
import java.util.Enumeration;

/**
 * @author luguosong
 */
public class ServletConfigDemo2 extends GenericServlet {
    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        /*
         * 对应web.xml中<servlet>标签中的<servlet-name>标签内容
         * */
        System.out.println(getServletName()); // servletContextDemo
        /*
         * 获取<servlet>标签下<init-param>标签信息
         * */
        Enumeration<String> names = getInitParameterNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            System.out.println(name + ":" + getInitParameter(name));
        }
    }
}
