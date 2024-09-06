package com.luguosong;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * @author luguosong
 */
@WebServlet("/addCookie")
public class AddCookie extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //创建Cookie
        Cookie cookie = new Cookie("key1", "helloCookie");

        //设置Cookie过期时间为10秒
        cookie.setMaxAge(10);

        // 您指定的目录中的所有页面以及该目录子目录中的所有页面都能看到 cookie。
        cookie.setPath(req.getContextPath());

        resp.addCookie(cookie);

        resp.sendRedirect(req.getContextPath()+"/hello_cookie.jsp");
    }
}
