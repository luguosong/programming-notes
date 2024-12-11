package com.luguosong.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author luguosong
 */
@Controller
@RequestMapping("/formString")
public class FormStringController {

    @RequestMapping("/springMvc")
    public String springMvc(
            @RequestBody String requestBody,
            HttpServletRequest request
    ) {
        request.setAttribute("username", requestBody);
        return "get-parameters/form";
    }
}
