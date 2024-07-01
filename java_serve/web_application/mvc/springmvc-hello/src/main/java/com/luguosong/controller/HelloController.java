package com.luguosong.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

/**
 * @author luguosong
 */
@Controller
public class HelloController {
    //请求映射
    @RequestMapping("/hello-mvc")
    public String hello() {
        //返回逻辑视图名称
        return "hello";
    }
}
