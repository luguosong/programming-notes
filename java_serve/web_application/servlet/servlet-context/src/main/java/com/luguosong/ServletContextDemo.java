package com.luguosong;

import jakarta.servlet.*;

import java.io.IOException;
import java.util.Enumeration;

/**
 * @author luguosong
 */
public class ServletContextDemo extends GenericServlet {
    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        /*
         * 获取ServletContext对象
         * */
        ServletContext context = getServletConfig().getServletContext(); //方式一
        context=getServletContext(); //方式二

        /*
        * 打印上下文初始化参数
        * 对应web.xml中的<context-param>标签内容
        * */
        Enumeration<String> names = context.getInitParameterNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            String value = context.getInitParameter(name);
            System.out.println(name + "=" + value);
        }

        /*
        * 获取应用的根路径
        * */
        System.out.println(context.getContextPath()); // /servlet_context_war_exploded

        /*
        * 获取文件的绝对路径
        * */
        System.out.println(context.getRealPath("/test.html")); //E:\IdeaCode\note\programming-study\docs\java_serve\web_application\servlet\servlet-context\target\servlet-context-1.0-SNAPSHOT\test.html

        /*
        * 会将日志记录到log目录下的localhost.xxxx-xx-xx.log日志中
        * */
        context.log("记录日志");
        context.log("记录异常",new RuntimeException("发生异常"));

        /*
        * 应用域操作
        * */
        context.setAttribute("key","value"); //存数据
        System.out.println(context.getAttribute("key")); //读数据
        context.removeAttribute("key"); //删数据
    }
}
