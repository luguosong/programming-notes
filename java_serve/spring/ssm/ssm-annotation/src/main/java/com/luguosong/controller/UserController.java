package com.luguosong.controller;

import com.luguosong.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author luguosong
 */
@Controller
public class UserController {

	@Autowired
	private UserService userService;

	@RequestMapping("/getUserById")
	public String getUserById(
			@RequestParam("id") Integer id,
			Model model) {

		model.addAttribute("user", userService.selectById(id));
		return "index";
	}
}
