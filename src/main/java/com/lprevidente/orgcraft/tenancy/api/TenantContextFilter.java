package com.lprevidente.orgcraft.tenancy.api;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TenantContextFilter extends OncePerRequestFilter {

  public static final String SESSION_TENANT_ATTRIBUTE = "orgcraft.tenantId";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain chain)
      throws ServletException, IOException {
    final var previous = TenantContext.get();
    final var session = request.getSession(false);
    if (session != null) {
      final var tenantId = (TenantId) session.getAttribute(SESSION_TENANT_ATTRIBUTE);
      if (tenantId != null) {
        TenantContext.set(tenantId);
      }
    }
    try {
      chain.doFilter(request, response);
    } finally {
      if (previous != null) {
        TenantContext.set(previous);
      } else {
        TenantContext.clear();
      }
    }
  }
}
