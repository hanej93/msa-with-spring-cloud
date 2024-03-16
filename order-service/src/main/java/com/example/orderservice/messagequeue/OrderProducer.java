package com.example.orderservice.messagequeue;

import java.util.Arrays;
import java.util.List;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.example.orderservice.dto.Field;
import com.example.orderservice.dto.KafkaOrderDto;
import com.example.orderservice.dto.OrderDto;
import com.example.orderservice.dto.Payload;
import com.example.orderservice.dto.Schema;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderProducer {

	private final KafkaTemplate<String, String> kafkaTemplate;

	private static final List<Field> fields = Arrays.asList(
		new Field("string", true, "order_id"),
		new Field("string", true, "user_id"),
		new Field("string", true, "product_id"),
		new Field("int32", true, "qty"),
		new Field("int32", true, "unit_price"),
		new Field("int32", true, "total_price")
	);

	private static final Schema schema = Schema.builder()
		.type("struct")
		.fields(fields)
		.optional(false)
		.name("orders")
		.build();

	public OrderDto send(String topic, OrderDto orderDto) {
		Payload payload = Payload.builder()
			.order_id(orderDto.getOrderId())
			.user_id(orderDto.getUserId())
			.product_id(orderDto.getProductId())
			.qty(orderDto.getQty())
			.unit_price(orderDto.getUnitPrice())
			.total_price(orderDto.getTotalPrice())
			.build();

		KafkaOrderDto kafkaOrderDto = KafkaOrderDto.builder()
			.schema(schema)
			.payload(payload)
			.build();

		ObjectMapper objectMapper = new ObjectMapper();
		String jsonInString = "";
		try {
			jsonInString = objectMapper.writeValueAsString(kafkaOrderDto);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		kafkaTemplate.send(topic, jsonInString);
		log.info("Kafka Producer sent data from the Order microservice: {}", kafkaOrderDto);
		return orderDto;
	}
}
