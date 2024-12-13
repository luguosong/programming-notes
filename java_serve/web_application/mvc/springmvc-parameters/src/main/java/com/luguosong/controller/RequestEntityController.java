package com.luguosong.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author luguosong
 */
@Controller
@RequestMapping("requestEntity")
public class RequestEntityController {

    @RequestMapping("/springMvc")
    public String springMvc(RequestEntity<String> requestEntity) {
        /*
         * 获取请求行信息
         * */
        System.out.println("请求方法：" + requestEntity.getMethod());
        System.out.println("请求地址：" + requestEntity.getUrl());

        /*
         * 获取请求头信息
         * */
        HttpHeaders headers = requestEntity.getHeaders();
        System.out.println("请求参数类型：" + headers.getContentType());

        /*
         * 获取请求体信息
         * */
        String entityBody = requestEntity.getBody();
        System.out.println("请求体内容：" + entityBody);
        return "get-parameters/form";
    }
}
