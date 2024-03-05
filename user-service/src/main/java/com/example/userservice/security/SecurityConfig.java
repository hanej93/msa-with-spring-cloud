package com.example.userservice.security;

import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.*;

import java.util.function.Supplier;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.util.matcher.IpAddressMatcher;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final AuthenticationManager authenticationManager;

	public static final String ALLOWED_IP_ADDRESS = "127.0.0.1";
	public static final String SUBNET = "/32";
	public static final IpAddressMatcher ALLOWED_IP_ADDRESS_MATCHER = new IpAddressMatcher(ALLOWED_IP_ADDRESS + SUBNET);

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() {
		return web -> web.ignoring()
			.requestMatchers(toH2Console());
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		// Configure AuthenticationManagerBuilder
		AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
		AuthenticationManager authenticationManager = authenticationManagerBuilder.build();

		http
			.csrf(AbstractHttpConfigurer::disable)
			.headers(httpSecurityHeadersConfigurer -> {
				httpSecurityHeadersConfigurer.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin);
			})
			.authorizeHttpRequests(auth -> {
				auth
					.requestMatchers("/**").permitAll()
					.requestMatchers("/**").access(
						new WebExpressionAuthorizationManager("hasIpAddress('127.0.0.1') or hasIpAddress('172.30.1.48')"));
			})
			.addFilter(getAuthenticationFilter(authenticationManager));

		return http.build();
	}

	private AuthorizationDecision hasIpAddress(Supplier<Authentication> authentication, RequestAuthorizationContext object) {
		return new AuthorizationDecision(ALLOWED_IP_ADDRESS_MATCHER.matches(object.getRequest()));
	}

	private AuthenticationFilter getAuthenticationFilter(AuthenticationManager authenticationManager) throws Exception {
		return new AuthenticationFilter(authenticationManager);
	}
}
