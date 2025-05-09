package org.example.genericservlet;

import java.io.*;

import jakarta.servlet.GenericServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;


public class HelloServlet extends GenericServlet {

    /*
    * 继承自GenericServlet，只需要实现service方法即可
    * */
    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        res.setContentType("text/html");
        PrintWriter out = res.getWriter();
        out.println("<h1>Hello GenericServlet!</h1>");
    }
}
