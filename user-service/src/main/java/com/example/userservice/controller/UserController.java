package com.example.userservice.controller;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.userservice.dto.UserDto;
import com.example.userservice.jpa.UserEntity;
import com.example.userservice.service.UserService;
import com.example.userservice.vo.Greeting;
import com.example.userservice.vo.RequestUser;
import com.example.userservice.vo.ResponseUser;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/user-service")
@RequiredArgsConstructor
public class UserController {

	private final Environment environment;
	private final Greeting greeting;

	private final UserService userService;

	@GetMapping("/health_check")
	public String status() {
		return String.format("It's Working in User Service on PORT %s", environment.getProperty("local.server.port"));
	}

	@GetMapping("/welcome")
	public String welcome() {
		// return environment.getProperty("greeting.message");
		return greeting.getMessage();
	}

	@PostMapping("/users")
	public ResponseEntity<ResponseUser> createUser(@RequestBody RequestUser requestUser) {
		ModelMapper modelMapper = new ModelMapper();
		modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

		UserDto userDto = modelMapper.map(requestUser, UserDto.class);
		userService.createUser(userDto);

		ResponseUser responseUser = modelMapper.map(userDto, ResponseUser.class);

		return new ResponseEntity<>(responseUser, HttpStatus.CREATED);
	}

	@GetMapping("/users")
	public ResponseEntity<List<ResponseUser>> gerUsers() {
		Iterable<UserEntity> users = userService.getUsers();

		List<ResponseUser> result = new ArrayList<>();
		users.forEach(v -> {
			result.add(new ModelMapper().map(v, ResponseUser.class));
		});

		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	@GetMapping("/users/{userId}")
	public ResponseEntity<ResponseUser> gerUser(@PathVariable("userId") String userId) {
		UserDto userDto = userService.getUserByUserId(userId);

		ResponseUser result = new ModelMapper().map(userDto, ResponseUser.class);

		return ResponseEntity.status(HttpStatus.OK).body(result);
	}
}
