package com.luguosong;

import jakarta.servlet.*;

import java.io.IOException;
import java.util.Enumeration;

/**
 * @author luguosong
 */
public class ServletConfigDemo extends GenericServlet {
    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        ServletConfig config = getServletConfig();
        /*
         * ServletConfig接口由Tomcat负责实现
         * 一个Servlet对象关联一个ServletConfig对象
         * */
        System.out.println(config); //org.apache.catalina.core.StandardWrapperFacade@280048c9

        /*
         * 对应web.xml中<servlet>标签中的<servlet-name>标签内容
         * */
        System.out.println(config.getServletName()); // servletContextDemo
        /*
         * 获取<servlet>标签下<init-param>标签信息
         * */
        Enumeration<String> names = config.getInitParameterNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            System.out.println(name + ":" + config.getInitParameter(name));
        }
    }
}
