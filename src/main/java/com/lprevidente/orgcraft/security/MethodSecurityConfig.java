package com.lprevidente.orgcraft.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Turns on {@code @PreAuthorize} and wires our {@link ResourceAuthorizationPermissionEvaluator} into
 * the {@code hasPermission(...)} SpEL function. The bean is {@code static} so the method-security
 * infrastructure can initialise it early without pulling in the rest of the context.
 */
@Configuration
@EnableMethodSecurity
class MethodSecurityConfig {

  @Bean
  static MethodSecurityExpressionHandler methodSecurityExpressionHandler(
      PermissionEvaluator permissionEvaluator) {
    final var handler = new DefaultMethodSecurityExpressionHandler();
    handler.setPermissionEvaluator(permissionEvaluator);
    return handler;
  }
}
