package com.lprevidente.orgcraft.security;

import com.lprevidente.orgcraft.tenancy.api.TenantContextFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Duration;

@Configuration
class SecurityConfig {

  @Bean
  SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      AuthHandler authHandler,
      TenantContextFilter tenantContextFilter,
      TenantResolvingPreAuthFilter tenantResolvingPreAuthFilter) {
    return http
        .securityMatcher("/**")
        .csrf(AbstractHttpConfigurer::disable)
        .formLogin(l -> l.usernameParameter("email").successHandler(authHandler).failureHandler(authHandler))
        .exceptionHandling(e -> e.authenticationEntryPoint(authHandler))
        .addFilterBefore(tenantContextFilter, SecurityContextHolderFilter.class)
        .addFilterBefore(tenantResolvingPreAuthFilter, UsernamePasswordAuthenticationFilter.class)
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/v1/organizations").permitAll()
            .requestMatchers("/api/**").authenticated()
            .anyRequest().permitAll())
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
