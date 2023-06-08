package com.example.userservice.controller;

import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.userservice.vo.Greeting;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class UserController {

	private final Environment environment;
	private final Greeting greeting;

	@GetMapping("/health_check")
	public String status() {
		return "It's Working in User Service";
	}

	@GetMapping("/welcome")
	public String welcome() {
		// return environment.getProperty("greeting.message");
		return greeting.getMessage();
	}

}