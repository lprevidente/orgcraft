package com.lprevidente.orgcraft.authorization;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

@TestConfiguration
public class AuthorizationTestConfig {

  @Bean("spiceDbAuthorizationManager")
  AuthorizationManager<RequestAuthorizationContext> permitAllSpiceDbAuthorizationManager() {
    return (authentication, context) -> new AuthorizationDecision(true);
  }
}
