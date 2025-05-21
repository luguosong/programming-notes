package com.luguosong.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author luguosong
 */
@Controller
@RequestMapping("/session-scope")
@SessionAttributes({"sessionScope"})
public class SessionScopeController {

    /*
    * 通过Servlet方式设置session
    * */
    @RequestMapping("/servlet")
    public String servletTest(HttpServletRequest request) {
        request.getSession().setAttribute("sessionScope", "通过Servlet原生方式设置session域");
        System.out.println(request.getSession().getAttribute("sessionScope"));
        return "session-scope";
    }

    /*
    * 通过@SessionAttributes注解设置session
    * */
    @RequestMapping("/modelAndView")
    public ModelAndView modelAndViewTest() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("sessionScope", "通过@SessionAttributes注解方式设置session域");
        modelAndView.setViewName("session-scope");
        return modelAndView;
    }
}
