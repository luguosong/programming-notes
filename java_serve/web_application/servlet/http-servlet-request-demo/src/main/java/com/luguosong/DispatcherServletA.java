package com.luguosong;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * @author luguosong
 */
public class DispatcherServletA extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("time", System.currentTimeMillis());

        // 请求转发
        // 参数：指定资源路径名的字符串。如果是相对路径，则必须与当前 servlet 相对。
        RequestDispatcher dispatcher = req.getRequestDispatcher("/dispatcherB");
        dispatcher.forward(req, resp);
    }
}
