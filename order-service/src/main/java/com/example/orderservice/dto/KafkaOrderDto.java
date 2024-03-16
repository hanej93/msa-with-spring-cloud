package com.example.orderservice.dto;

import java.io.Serializable;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KafkaOrderDto implements Serializable {
	private Schema schema;
	private Payload payload;
}
