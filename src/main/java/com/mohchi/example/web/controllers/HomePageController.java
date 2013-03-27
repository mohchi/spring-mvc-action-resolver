package com.mohchi.example.web.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.mohchi.example.web.framework.Action;
import com.mohchi.example.web.framework.ActionUriBuilder;

@Controller
public class HomePageController {

	@RequestMapping("/")
	@Action // Uses the method name.
	public String homePage() {
		return "homePage";
	}

	@RequestMapping("/renamedPage")
	@Action("RENAMED_PAGE") // Uses the given name.
	public String renamedPage() {
		return "homePage";
	}

	@RequestMapping("/pathVariablePage/{var1:[A-Za-z0-9]+}/{var2:[A-Za-z0-9]+}")
	@Action
	public String pathVariablePage(@PathVariable("var") String var) {
		return "homePage";
	}

	@RequestMapping("/notMapped")
	public String notMappedPage() {
		return "homePage";
	}

	@RequestMapping("/redirect")
	@Action
	public String redirect() {
		return "redirect:" + ActionUriBuilder.internalActionUri("homePage");
	}

}
