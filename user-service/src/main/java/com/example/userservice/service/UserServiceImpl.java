package com.example.userservice.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.core.env.Environment;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.userservice.client.OrderServiceClient;
import com.example.userservice.dto.UserDto;
import com.example.userservice.jpa.UserEntity;
import com.example.userservice.jpa.UserRepository;
import com.example.userservice.vo.ResponseOrder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final RestTemplate restTemplate;
	private final Environment env;
	private final OrderServiceClient orderServiceClient;
	private final CircuitBreakerFactory circuitBreakerFactory;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		UserEntity userEntity = userRepository.findByEmail(username)
			.orElseThrow(() -> new UsernameNotFoundException(username));

		return new User(userEntity.getEmail(), userEntity.getEncryptedPwd(),
			true, true, true, true,
			new ArrayList<>());
	}

	@Override
	public UserDto createUser(UserDto userDto) {
		userDto.setUserId(UUID.randomUUID().toString());

		ModelMapper modelMapper = new ModelMapper();
		modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		UserEntity userEntity = modelMapper.map(userDto, UserEntity.class);
		userEntity.setEncryptedPwd(passwordEncoder.encode(userDto.getPwd()));

		userRepository.save(userEntity);

		return userDto;
	}

	@Override
	public UserDto getUserByUserId(String userId) {
		UserEntity userEntity = userRepository.findByUserId(userId)
			.orElseThrow(() -> new UsernameNotFoundException("User not found"));

		UserDto userDto = new ModelMapper().map(userEntity, UserDto.class);

		// List<ResponseOrder> orders = new ArrayList<>();

		/* Using as restTemplate */
		// String orderUrl = String.format(env.getProperty("order-service.url"), userId);
		// ResponseEntity<List<ResponseOrder>> orderListResponse = restTemplate.exchange(orderUrl, HttpMethod.GET, null,
		// 	new ParameterizedTypeReference<List<ResponseOrder>>() {});
		// List<ResponseOrder> orders = orderListResponse.getBody();

		/* Using as FeignClient */
		/* Feign exception handling */
		// List<ResponseOrder> orders = null;
		// try {
		// 	orders = orderServiceClient.getOrders(userId);
		// } catch (FeignException e) {
		// 	log.error(e.getLocalizedMessage());
		// }

		/* Error Decoder*/
		// List<ResponseOrder> orders = orderServiceClient.getOrders(userId);
		log.info("Before call orders microservice");
		CircuitBreaker circuitBreaker = circuitBreakerFactory.create("circuitBreaker");
		List<ResponseOrder> orders = circuitBreaker.run(() -> orderServiceClient.getOrders(userId),
			throwable -> new ArrayList<>());
		log.info("After call orders microservice");

		userDto.setOrders(orders);

		return userDto;
	}

	@Override
	public Iterable<UserEntity> getUsers() {
		return userRepository.findAll();
	}

	@Override
	public UserDto getUserDetailsByEmail(String email) {
		UserEntity userEntity = userRepository.findByEmail(email)
			.orElseThrow(() -> new UsernameNotFoundException(email));
		return new ModelMapper().map(userEntity, UserDto.class);
	}
}
