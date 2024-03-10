package com.example.userservice.error;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FeignErrorDecoder implements ErrorDecoder {

	private final Environment env;

	@Override
	public Exception decode(String methodKey, Response response) {
		switch (response.status()) {
			case 400:
				break;
			case 404:
				if (methodKey.contains("getOrders")) {
					return new ResponseStatusException(HttpStatusCode.valueOf(response.status()),
						env.getProperty("order-service.exception.orders-is-empty"));
				}
				break;
			default:
				return new Exception(response.reason());
		}

		return null;
	}
}
