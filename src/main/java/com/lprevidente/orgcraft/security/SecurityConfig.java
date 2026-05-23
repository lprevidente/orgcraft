package com.lprevidente.orgcraft.security;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(
			HttpSecurity http,
			AuthHandler authHandler,
			@Qualifier("spiceDbAuthorizationManager")
					AuthorizationManager<RequestAuthorizationContext> spiceDbAuthorizationManager) {
		return http
				.securityMatcher("/**")
				.csrf(AbstractHttpConfigurer::disable)
				.formLogin(l -> l.usernameParameter("email").successHandler(authHandler).failureHandler(authHandler))
				.exceptionHandling(e -> e.authenticationEntryPoint(authHandler))
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/v1/organizations").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/v1/teams/{id}").access(spiceDbAuthorizationManager)
						.anyRequest().authenticated())
				.build();
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		final var source = new UrlBasedCorsConfigurationSource();
		final var config = new CorsConfiguration();
		config.addAllowedHeader("*");
		config.addAllowedMethod("*");
		config.addAllowedOriginPattern("*");
		config.setAllowCredentials(true);
		config.applyPermitDefaultValues();
		config.setMaxAge(Duration.ofDays(1));
		source.registerCorsConfiguration("/**", config);
		return source;
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}
