package com.luguosong;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * @author luguosong
 */
@WebServlet("/deleteSession")
public class DeleteSession extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        /*
        * 删除session
        * */
        //req.getSession().removeAttribute("s");
        //或者
        req.getSession().invalidate();

        //重定向回JSP页面
        resp.sendRedirect(req.getContextPath() + "/hello_session.jsp");
    }
}
