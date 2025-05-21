package com.luguosong.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author luguosong
 */
@Controller
@RequestMapping("/application-scope")
public class ApplicationScopeController {

    @RequestMapping("/servlet")
    public String servletTst(HttpServletRequest request) {
        request.getServletContext().setAttribute("applicationScope", "通过Servlet方式设置application域");
        return "application-scope";
    }
}
