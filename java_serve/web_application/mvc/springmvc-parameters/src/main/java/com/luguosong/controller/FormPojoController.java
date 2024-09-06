package com.luguosong.controller;

import com.luguosong.pojo.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author luguosong
 */
@Controller
@RequestMapping("/formPojo")
public class FormPojoController {

    @PostMapping("/springMvc")
    public String springMvcPojo(
            User user,
            HttpServletRequest request) {
        request.setAttribute("username", user.getUsername());
        return "get-parameters/form";
    }
}
