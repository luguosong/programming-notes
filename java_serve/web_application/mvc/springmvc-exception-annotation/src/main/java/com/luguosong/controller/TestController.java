package com.luguosong.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author luguosong
 */
@RestController
public class TestController {

    @PostMapping("/test")
    public String test() {
        return "test";
    }
}
