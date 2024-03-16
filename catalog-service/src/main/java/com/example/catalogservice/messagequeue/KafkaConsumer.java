package com.example.catalogservice.messagequeue;

import java.util.HashMap;
import java.util.Map;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.example.catalogservice.jpa.CatalogEntity;
import com.example.catalogservice.jpa.CatalogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumer {

	private final CatalogRepository catalogRepository;

	@Transactional
	@KafkaListener(topics = "example-catalog-topic")
	public void updateQty(String kafkaMessage) {
		log.info("Kafka Message: -> " + kafkaMessage);

		Map<Object, Object> map = new HashMap<>();
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			map = objectMapper.readValue(kafkaMessage, new TypeReference<Map<Object, Object>>() {});
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		CatalogEntity catalogEntity = catalogRepository.findByProductId((String)map.get("productId"))
			.orElseThrow(() -> new RuntimeException());

		catalogEntity.setStock(catalogEntity.getStock() - (Integer)map.get("qty"));
	}
}
