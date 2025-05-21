package com.luguosong.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

/**
 * @author luguosong
 */
@Controller
@RequestMapping("/request-scope")
public class RequestScopeController {

    /**
     * 模拟Servlet存储Request域
     */
    @RequestMapping("/servlet")
    public String servletTest(HttpServletRequest request) {
        request.setAttribute("requestScope", "通过HttpServletRequest设置请求域");
        return "request-scope";
    }

    /*
     * Model对象存储request域
     * */
    @RequestMapping("/model")
    public String modelTest(Model model) {
        model.addAttribute("requestScope", "通过Model对象设置请求域");
        return "request-scope";
    }

    /*
     * Map集合存储request域
     * */
    @RequestMapping("/map")
    public String mapTest(Map<String, Object> map) {
        map.put("requestScope", "通过Map对象设置请求域");
        return "request-scope";
    }

    /*
     * ModelMap对象存储request域
     * */
    @RequestMapping("/modelMap")
    public String modelMapTest(ModelMap modelMap) {
        modelMap.addAttribute("requestScope", "通过ModelMap对象设置请求域");
        return "request-scope";
    }

    /*
     * ⭐ModelAndView对象存储request域
     * */
    @RequestMapping("/modelAndView")
    public ModelAndView modelAndViewTest() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("requestScope", "通过ModelAndView对象设置请求域");
        modelAndView.setViewName("request-scope");
        return modelAndView;
    }


}
