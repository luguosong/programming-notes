package com.luguosong.ssiach6ex4.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HelloController {

    @GetMapping("/home")
    public String home() {
        return "home.html";
    }

}
