package com.luguosong.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author luguosong
 */
@Controller

public class TestController {
    /*
     * 执行get方法，表示查询
     * */
    @RequestMapping(value = "user", method = RequestMethod.GET)
    public String get() {
        System.out.println("执行get方法");
        return "test";
    }

    /*
     * 执行post方法，表示新增
     * */
    @RequestMapping(value = "user", method = RequestMethod.POST)
    public String post() {
        System.out.println("执行post方法");
        return "test";
    }

    /*
     * 执行put方法，表示修改
     * */
    @RequestMapping(value = "user", method = RequestMethod.PUT)
    public String put() {
        System.out.println("执行put方法");
        return "test";
    }

    /*
     * 执行delete方法，表示删除
     * */
    @RequestMapping(value = "user", method = RequestMethod.DELETE)
    public String delete() {
        System.out.println("执行delete方法");
        return "test";
    }
}
