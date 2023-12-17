package com.example.spring_security_hello.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author luguosong
 */
@RestController
public class TestController {

    /**
     * 测试接口
     *
     * @return
     */
    @GetMapping("/test")
    public String test() {
        return "hello world";
    }
}
