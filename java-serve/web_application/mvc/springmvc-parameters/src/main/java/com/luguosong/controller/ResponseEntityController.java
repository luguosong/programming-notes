package com.luguosong.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author luguosong
 */
@Controller
@RequestMapping("/responseEntity")
public class ResponseEntityController {
    @RequestMapping("/springmvc")
    public ResponseEntity<String> springMvc() {
        /*
         * 模拟响应404
         * */
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }
}
