package com.luguosong.controller;

import com.luguosong.pojo.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author luguosong
 */
@Controller
@RequestMapping("return-json-string")
public class ResponseJSONStringController {

    /*
     * 返回json字符串
     * */
    @RequestMapping("user")
    @ResponseBody
    public User getUser() {
        User user = new User();
        user.setUsername("luguosong");
        return user;
    }
}
