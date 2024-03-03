package com.example.userservice.jpa;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<UserEntity, Long> {
	Optional<UserEntity> findByUserId(String userId);
}
