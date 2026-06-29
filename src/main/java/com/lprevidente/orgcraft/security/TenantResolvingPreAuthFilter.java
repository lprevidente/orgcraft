package com.lprevidente.orgcraft.security;

import com.lprevidente.orgcraft.organization.api.OrganizationApi;
import com.lprevidente.orgcraft.tenancy.api.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
class TenantResolvingPreAuthFilter extends OncePerRequestFilter {

  private static final String LOGIN_PATH = "/login";
  private static final String TENANT_PARAM = "tenant";

  private final OrganizationApi organizations;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain chain)
      throws ServletException, IOException {
    if ("POST".equals(request.getMethod()) && LOGIN_PATH.equals(request.getRequestURI())) {
      final var slug = request.getParameter(TENANT_PARAM);
      if (slug != null && !slug.isBlank()) {
        organizations
            .findIdBySlug(slug)
            .ifPresent(id -> TenantContext.set(id.id().toString()));
      }
    }
    chain.doFilter(request, response);
  }
}
