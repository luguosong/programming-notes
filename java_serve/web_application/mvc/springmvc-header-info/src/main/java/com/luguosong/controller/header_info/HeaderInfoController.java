package com.luguosong.controller.header_info;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author luguosong
 */
@Controller
@RequestMapping("/header-info")
public class HeaderInfoController {

    @PostMapping("/springMvc")
    public String springMvc(@RequestHeader(value = "content-type", required = false) String contentType) {
        System.out.println(contentType);
        return "header-info/form";
    }
}
