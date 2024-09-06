package com.luguosong;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebInitParam;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author luguosong
 */
@WebServlet(urlPatterns = {"/helloServlet"},
        initParams = {@WebInitParam(name = "name", value = "张三")})
public class HelloServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("content-type", "text/html;charset=UTF-8");
        String name = getInitParameter("name");
        PrintWriter writer = resp.getWriter();
        writer.println("获取Servlet初始化参数：" + name);
    }
}
