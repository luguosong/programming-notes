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
@WebServlet("/deleteCookie")
public class DeleteCookie extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Cookie cookie = new Cookie("key1", "");
        //将有效期设置为0，等价于清除同名Cookie
        cookie.setMaxAge(0);
        resp.addCookie(cookie);
        resp.sendRedirect(req.getContextPath()+"/hello_cookie.jsp");
    }
}
