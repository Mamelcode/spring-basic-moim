package org.edupoll.controller;

import org.edupoll.model.dto.user.JoinUserRequestData;
import org.edupoll.model.dto.user.LoginRequestData;
import org.edupoll.model.entity.User;
import org.edupoll.security.support.AccountManager;
import org.edupoll.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import jakarta.servlet.http.HttpSession;

@Controller
public class UserController {
	
	Logger logger = LoggerFactory.getLogger(UserController.class);
	
	@Autowired
	UserService userService;
	
	@Autowired
	AccountManager accountManager;
	
	@GetMapping("/join")
	public String joinViewHandle() {
		
		return "user/join";
	}
	
	@PostMapping("/join-task")
	public String joinTaskHandle(JoinUserRequestData user, Model model) {
		boolean result = accountManager.createUser(user);
		System.out.println("회원가입 여부 ==> "+ result);
		logger.debug("userJoinHandle : {} ", result);
		if(result) {
			return "redirect:/user/login";
		}else {
			model.addAttribute("error", true);
			return "user/join";
		}
	}
	
	@GetMapping("/login")
	public String loginViewHandle(@RequestParam(required = false) String error ,Model model) {
		
		if(error != null) {
			model.addAttribute("error", true);
		}
		return "user/login";
	}
}
