package com.luguosong.ssiach11ex1.controllers;


import com.luguosong.ssiach11ex1.services.NameService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    private final NameService nameService;

    public HelloController(NameService nameService) {
        this.nameService = nameService;
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello, " + nameService.getName();
    }
}
