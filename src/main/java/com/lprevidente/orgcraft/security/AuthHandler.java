package com.lprevidente.orgcraft.security;

import com.lprevidente.orgcraft.tenancy.api.TenantContext;
import com.lprevidente.orgcraft.tenancy.api.TenantContextFilter;
import com.lprevidente.orgcraft.user.api.UserApi;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
class AuthHandler implements AuthenticationSuccessHandler, AuthenticationFailureHandler,
    AuthenticationEntryPoint, UserDetailsService {

  private final JsonMapper mapper;
  private final UserApi userApi;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    return userApi.findByEmail(email, UserDetailsView.class)
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
  }

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest req,
      HttpServletResponse res,
      Authentication authentication) throws IOException {

    final var tenantId = TenantContext.get();
    if (tenantId != null) {
      req.getSession(true).setAttribute(TenantContextFilter.SESSION_TENANT_ATTRIBUTE, tenantId);
    }

    res.setContentType(MediaType.APPLICATION_JSON_VALUE);
    final var authRes = new AuthRes(true, null);
    mapper.writeValue(res.getWriter(), authRes);
  }

  @Override
  public void onAuthenticationFailure(
      HttpServletRequest req,
      HttpServletResponse res,
      AuthenticationException ex) throws IOException {
    res.setStatus(HttpStatus.UNAUTHORIZED.value());
    res.setContentType(MediaType.APPLICATION_JSON_VALUE);

    final var authRes = new AuthRes(false, ex.getMessage());
    mapper.writeValue(res.getWriter(), authRes);
  }

  @Override
  public void commence(
      HttpServletRequest req,
      HttpServletResponse res,
      AuthenticationException authException) throws IOException {
    res.setStatus(HttpStatus.UNAUTHORIZED.value());
    res.setContentType(MediaType.APPLICATION_JSON_VALUE);

    final var authRes = new AuthRes(false, "Authentication required");
    mapper.writeValue(res.getWriter(), authRes);
  }


}
