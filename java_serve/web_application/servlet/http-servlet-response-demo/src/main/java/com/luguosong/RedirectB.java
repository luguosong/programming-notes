package com.luguosong;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * @author luguosong
 */
public class RedirectB extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        resp.setHeader("content-type", "text/html;charset=UTF-8");

        resp.getWriter().println("重定向无法获取请求域：" + req.getAttribute("time"));
    }
}
