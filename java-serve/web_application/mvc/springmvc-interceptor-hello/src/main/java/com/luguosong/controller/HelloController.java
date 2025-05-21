package com.luguosong.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author luguosong
 */
@RestController
public class HelloController {
    //请求映射
    @RequestMapping("/hello-interceptor")
    public String hello() {
        System.out.println("Controller方法执行");
        //返回逻辑视图名称
        return "hello interceptor";
    }

    @RequestMapping("/hello-interceptor2")
    public String hello2() {
        System.out.println("Controller方法执行，拦截器不进行拦截");
        return "hello interceptor2";
    }
}
