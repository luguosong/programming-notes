package com.example.sessionhello.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author luguosong
 */
@RestController
public class SessionController {
    @GetMapping("/session/set")
    public String setSession(HttpServletRequest request) {
        request.getSession().setAttribute("user", "luguosong");
        return "session设置成功";
    }

    @GetMapping("/session/get")
    public String getSession(HttpServletRequest request) {
        return "Session为：" + request.getSession().getAttribute("user");
    }

    @GetMapping("/session/remove")
    public String removeSession(HttpServletRequest request) {
        request.getSession().removeAttribute("user");
        return "session删除成功";
    }
}
