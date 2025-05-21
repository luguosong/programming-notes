package com.luguosong.controller;

import com.luguosong.pojo.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author luguosong
 */
@Controller
@RequestMapping("jsonPojo")
public class JsonPojoController {
    @RequestMapping("springMvc")
    public String springMvc(
            @RequestBody User user) {
        System.out.println(user);
        return "get-parameters/form";
    }
}
