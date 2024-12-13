package com.luguosong;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Map;

/**
 * @author luguosong
 */
public class GetRequestInfo extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter writer = resp.getWriter();


        //解决响应中文乱码
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/plain");

        /*
         * 请求域操作
         *
         * 请求域只在一次请求内有效
         * */
        req.setAttribute("name", "luguosong");
        req.getAttribute("name");
        req.removeAttribute("name");


        /*
         * 获取客户端信息
         * */
        String remoteAddr = req.getRemoteAddr(); //获取客户端ip地址
        writer.println("客户端ip地址：" + remoteAddr);


        String contextPath = req.getContextPath(); // 获取应用路径
        writer.println("应用路径：" + contextPath);
        // 当但也可以通过ServletContext获取
        String contextPath2 = getServletContext().getContextPath();
        writer.println("应用路径：" + contextPath2);
        // 获取Servlet路径，相比于getRequestURI不带应用路径
        String servletPath = req.getServletPath();
        writer.println("Servlet路径：" + servletPath);


        //////////////////////////////////////////////////////////////////////////////


        /*
         * 获取请求行信息
         * */
        String httpMethod = req.getMethod(); //获取请求方法
        writer.println("请求方法：" + httpMethod);
        String requestURI = req.getRequestURI(); //获取请求路径
        writer.println("URI：" + requestURI);
        String protocol = req.getProtocol(); // 获取Http协议版本
        writer.println("协议：" + protocol);


        /*
        * 获取请求头信息
        * */
        String accept = req.getHeader("Accept"); // 参数为指定头名称
        writer.println("Accept：" + accept);

        /*
         * 表单请求参数获取
         * 获取application/x-www-form-urlencoded类型的数据
         * */
        Map<String, String[]> parameterMap = req.getParameterMap(); //获取参数Map集合
        Enumeration<String> names = req.getParameterNames(); //获取参数名的枚举
        String[] values1 = req.getParameterValues("name"); //获取指定参数名的参数值数组
        writer.println("获取参数值数组：" + Arrays.toString(values1));
        String value2 = req.getParameter("name"); // 🔥获取指定参数名的参数值
        writer.println("获取参数:" + value2);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        /*
         * 解决Post请求中文乱码
         *
         * ❗Get请求没有乱码问题，Get请求参数默认使用UTF-8编码
         * */
        req.setCharacterEncoding("UTF-8");
    }
}
