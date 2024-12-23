package com.luguosong;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author luguosong
 */
public class OutputStreamDemo extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String data = "使用OutputStream流向浏览器输出中文";
        ServletOutputStream outputStream = resp.getOutputStream();
        resp.setHeader("content-type", "text/html;charset=UTF-8");
        byte[] dataByteArr = data.getBytes(StandardCharsets.UTF_8);
        outputStream.write(dataByteArr);
    }
}
