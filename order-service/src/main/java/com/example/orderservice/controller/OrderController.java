package com.example.orderservice.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
import com.example.orderservice.messagequeue.KafkaProducer;
import com.example.orderservice.messagequeue.OrderProducer;
import com.example.orderservice.service.OrderService;
import com.example.orderservice.vo.RequestOrder;
import com.example.orderservice.vo.ResponseOrder;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/order-service")
@RequiredArgsConstructor
public class OrderController {

	private final Environment env;
	private final OrderService orderService;
	private final KafkaProducer kafkaProducer;
	private final OrderProducer orderProducer;


	@GetMapping("/health_check")
	public String status() {
		return String.format("It's Working in Order Service on PORT %s", env.getProperty("local.server.port"));
	}

	@PostMapping("/{userId}/orders")
	public ResponseEntity<ResponseOrder> createOrder(@PathVariable("userId") String userId,
													@RequestBody RequestOrder orderDetails) {
		ModelMapper modelMapper = new ModelMapper();
		modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

		OrderDto orderDto = modelMapper.map(orderDetails, OrderDto.class);
		orderDto.setUserId(userId);

		/* JPA */
		orderService.createOrder(orderDto);
		ResponseOrder responseOrder = modelMapper.map(orderDto, ResponseOrder.class);

		// /* kafka */
		// orderDto.setOrderId(UUID.randomUUID().toString());
		// orderDto.setTotalPrice(orderDetails.getQty() * orderDetails.getUnitPrice());
		//
		// /* send this order to the kafka */
		// kafkaProducer.send("example-catalog-topic", orderDto);
		// orderProducer.send("orders", orderDto);

		// ResponseOrder responseOrder = modelMapper.map(orderDto, ResponseOrder.class);

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
