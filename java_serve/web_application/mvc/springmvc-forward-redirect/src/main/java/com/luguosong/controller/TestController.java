package com.luguosong.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author luguosong
 */
@Controller
public class TestController {

    /*
    * 转发
    * */
    @RequestMapping("forward")
    public String forwardTest() {
        return "forward:/test";
    }

    /*
    * 重定向
    * */
    @RequestMapping("redirect")
    public String redirectTest() {
        return "redirect:/test";
    }

    @RequestMapping("/test")
    public String test() {
        return "test";
    }
}
