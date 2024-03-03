package com.example.orderservice.controller;

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

import com.example.orderservice.dto.OrderDto;
import com.example.orderservice.jpa.OrderEntity;
import com.example.orderservice.service.OrderService;
import com.example.orderservice.vo.RequestOrder;
import com.example.orderservice.vo.ResponseOrder;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/order-service")
@RequiredArgsConstructor
public class OrderController {

	private final Environment environment;
	private final OrderService orderService;


	@GetMapping("/health_check")
	public String status() {
		return String.format("It's Working in Order Service on PORT %s", environment.getProperty("local.server.port"));
	}

	@PostMapping("/{userId}/orders")
	public ResponseEntity<ResponseOrder> createOrder(@PathVariable("userId") String userId,
													@RequestBody RequestOrder orderDetails) {
		ModelMapper modelMapper = new ModelMapper();
		modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

		OrderDto orderDto = modelMapper.map(orderDetails, OrderDto.class);
		orderDto.setUserId(userId);
		orderService.createOrder(orderDto);

		ResponseOrder responseOrder = modelMapper.map(orderDto, ResponseOrder.class);

		return new ResponseEntity<>(responseOrder, HttpStatus.CREATED);
	}

	@GetMapping("/{userId}/orders")
	public ResponseEntity<List<ResponseOrder>> getOrder(@PathVariable("userId") String userId) {
		Iterable<OrderEntity> orders = orderService.getOrdersByUserId(userId);

		List<ResponseOrder> result = new ArrayList<>();
		orders.forEach(v -> {
			result.add(new ModelMapper().map(v, ResponseOrder.class));
		});

		return ResponseEntity.ok(result);
	}
}
