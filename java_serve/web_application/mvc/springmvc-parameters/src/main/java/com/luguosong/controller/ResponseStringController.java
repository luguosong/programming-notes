package com.luguosong.controller;

import com.luguosong.pojo.User;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author luguosong
 */
@Controller
@RequestMapping("return-string")
public class ResponseStringController {

    /*
     * 模拟servlet返回String字符串
     *
     * 理论上应该返回逻辑视图名称，但此处返回null
     * 通过HttpServletResponse做出响应
     * */
    @RequestMapping("/servlet")
    public String servlet(HttpServletResponse resp) throws IOException {
        resp.setHeader("content-type", "text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        out.println("servlet输出字符串");
        return null;
    }


    /*
     * 模拟springMvc返回String字符串
     *
     * 添加@ResponseBody后，返回的不再是逻辑视图名称
     * 而是直接返回text/html
     * */
    @RequestMapping(value = "/springMvc", produces = "text/html; charset=utf-8")
    @ResponseBody
    public String springMvc() {
        return "Spring MVC输出字符串";
    }
}
