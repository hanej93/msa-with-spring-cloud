package com.example.orderservice.jpa;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface OrderRepository extends CrudRepository<OrderEntity, Long> {

	Optional<OrderEntity> findByOrderId(String orderId);

	Iterable<OrderEntity> findByUserId(String userId);
}
