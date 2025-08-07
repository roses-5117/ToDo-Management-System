package com.dmm.task;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class TestController {

	@RequestMapping("/testcreate")
	public String index() {
		return "create";
	}

	@RequestMapping("/testedit")
	public String test() {
		return "edit";
	}

	@RequestMapping("/testmain")
	public String main() {
		return "main";
	}

	@RequestMapping("/testlogin")
	public String login() {
		return "login";
	}

}