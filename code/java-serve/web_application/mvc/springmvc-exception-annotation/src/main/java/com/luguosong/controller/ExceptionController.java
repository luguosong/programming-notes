package com.luguosong.controller;


import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * @author luguosong
 */
@ControllerAdvice
public class ExceptionController {

    @ExceptionHandler
    public String error(Exception e, Model model){
        model.addAttribute("errMsg", e);
        return "error";
    }
}
