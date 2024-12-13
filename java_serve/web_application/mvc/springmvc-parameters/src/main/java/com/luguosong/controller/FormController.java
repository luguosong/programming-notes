package com.luguosong.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author luguosong
 */
@Controller
@RequestMapping("/form")
public class FormController {

    @GetMapping("/formView")
    public String formView() {
        return "get-parameters/form";
    }

    /*
     * 模拟Servlet接收参数
     * */
    @PostMapping("/servlet")
    public String servletPost(HttpServletRequest request) {
        request.setAttribute("username", request.getParameter("username"));
        return "get-parameters/form";
    }

    /*
     * @RequestParam注解value值为请求参数名
     * required属性表示参数是否必须，默认为true
     * defaultValue属性为默认值
     * */
    @PostMapping("/springMvc")
    public String springMvc(
            @RequestParam(
                    value = "username",
                    required = false,
                    defaultValue = "张三") String username,
            HttpServletRequest request) {
        request.setAttribute("username", username);
        return "get-parameters/form";
    }


    /*
     * 如果请求参数的形参名和方法参数名一样，则@RequestParam注解可以省略
     * */
    @PostMapping("/springMvc2")
    public String springMvc2(
            String username,
            HttpServletRequest request) {
        request.setAttribute("username", username);
        return "get-parameters/form";
    }
}
