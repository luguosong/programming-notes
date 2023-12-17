package com.example.cookiehello.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * @author luguosong
 */
@RestController
public class TestController {

    /*
     * 设置Cookie
     * */
    @GetMapping("login")
    public String login(HttpServletResponse response) throws UnsupportedEncodingException {
        //⚠️Cookie不支持中文存储，可以将中文先进行Url编码
        Cookie cookie = new Cookie("name", URLEncoder.encode("张三", StandardCharsets.UTF_8));

        /*
         * 设置为正数表示Cookie销毁时间（浏览器关闭不会清除Cookie）
         * 设置为0，表示删除Cookie
         * 设置为负数表示关闭浏览器销毁Cookie
         * */
        cookie.setMaxAge(60);

        response.addCookie(cookie);

        return "Cookie设置成功";
    }

    /*
     * 接收Cookie
     * */
    @GetMapping("test")
    public String test(@CookieValue(name = "myCookie", required = false) String myCookieValue) {
        return "当前Cookie为：" + myCookieValue;
    }
}
