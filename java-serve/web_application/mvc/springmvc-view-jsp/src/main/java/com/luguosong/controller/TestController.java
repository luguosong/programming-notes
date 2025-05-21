package com.luguosong.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author luguosong
 */
@Controller
public class TestController {

    @RequestMapping("/test")
    public String test() {
        return "test";
    }
}
