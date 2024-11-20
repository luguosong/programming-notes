package com.luguosong;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author luguosong
 */
public class WriteDemo extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String data = "使用PrintWriter流输出中文";
        PrintWriter out = resp.getWriter();
        resp.setCharacterEncoding("UTF-8"); //Tomcat10默认使用UTF-8，这一步可以省略
        resp.setHeader("content-type", "text/html;charset=UTF-8");
        //或者
        //resp.setContentType("text/html;charset=UTF-8");
        out.write(data);
    }
}
