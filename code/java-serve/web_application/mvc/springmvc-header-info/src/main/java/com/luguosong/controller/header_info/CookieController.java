package com.luguosong.controller.header_info;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author luguosong
 */
@Controller
@RequestMapping("/cookie")
public class CookieController {

    /*
     * ❗cookie需要在父路径相同的情况才能保存下来
     * 因此通过该地址访问视图
     * */
    @GetMapping("/view")
    public String view() {
        return "header-info/form";
    }

    @RequestMapping("/springMvc")
    public String springMvc(@CookieValue("username") String username) {
        System.out.println(username);
        return "header-info/form";
    }
}
