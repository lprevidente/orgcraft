package com.lprevidente.orgcraft.security;

import com.lprevidente.orgcraft.BaseIntegrationTest;
import com.lprevidente.orgcraft.tenancy.api.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

@Sql(
    value = {"/organizations.sql", "/users.sql"},
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class SecurityConfigIntegrationTest extends BaseIntegrationTest {

  private static final String EMAIL = "mario.rossi@example.com";
  private static final String PASSWORD = "Test@1234";

  @Nested
  @DisplayName("Unauthenticated access")
  class UnauthenticatedAccess {

    @Test
    @DisplayName("Should return 401 with AuthRes JSON on protected endpoint")
    void shouldReturnUnauthorizedOnProtectedEndpoint() {
      mockMvcTester
          .get()
          .uri("/api/v1/users")
          .exchange()
          .assertThat()
          .hasStatus(HttpStatus.UNAUTHORIZED)
          .hasContentType(MediaType.APPLICATION_JSON)
          .bodyJson()
          .extractingPath("$.signed")
          .asBoolean()
          .isFalse();
    }

    @Test
    @DisplayName("Should include 'Authentication required' reason")
    void shouldIncludeAuthenticationRequiredReason() {
      mockMvcTester
          .get()
          .uri("/api/v1/users")
          .exchange()
          .assertThat()
          .hasStatus(HttpStatus.UNAUTHORIZED)
          .bodyJson()
          .extractingPath("$.reason")
          .asString()
          .isEqualTo("Authentication required");
    }

    @Test
    @DisplayName("Should permit OPTIONS preflight without authentication")
    void shouldPermitOptionsWithoutAuthentication() {
      mockMvcTester
          .options()
          .uri("/api/v1/users")
          .exchange()
          .assertThat()
          .hasStatusOk();
    }

    @Test
    @DisplayName("Should require authentication on POST /api/v1/users")
    void shouldRequireAuthenticationOnCreateUser() {
      mockMvcTester
          .post()
          .uri("/api/v1/users")
          .contentType(MediaType.APPLICATION_JSON)
          .content("{\"firstName\":\"X\",\"lastName\":\"Y\",\"email\":\"x@y.com\",\"password\":\"Password@123\"}")
          .exchange()
          .assertThat()
          .hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Should permit POST /api/v1/organizations without authentication")
    void shouldPermitRegisterOrganizationWithoutAuthentication() {
      mockMvcTester
          .post()
          .uri("/api/v1/organizations")
          .contentType(MediaType.APPLICATION_JSON)
          .content(
              "{\"name\":\"Unauthed Co\",\"slug\":\"unauthed-co\",\"founderFirstName\":\"U\",\"founderLastName\":\"A\",\"founderEmail\":\"u@a.com\",\"founderPassword\":\"Password@123\"}")
          .exchange()
          .assertThat()
          .hasStatus(HttpStatus.CREATED);
    }
  }

  @Nested
  @DisplayName("POST /login")
  class Login {

    @BeforeEach
    void clearTenantBeforeLogin() {
      // Login tests must exercise the real resolution from the `tenant` form field; the base
      // class's pre-set TenantContext would mask "missing/unknown tenant" failure paths.
      TenantContext.clear();
    }

    @Test
    @DisplayName("Should return 200 with signed=true on valid credentials and tenant")
    void shouldReturnOkOnValidCredentials() {
      mockMvcTester
          .post()
          .uri("/login")
          .contentType(MediaType.APPLICATION_FORM_URLENCODED)
          .param("email", EMAIL)
          .param("password", PASSWORD)
          .param("tenant", TEST_TENANT_SLUG)
          .exchange()
          .assertThat()
          .hasStatusOk()
          .hasContentType(MediaType.APPLICATION_JSON)
          .bodyJson()
          .extractingPath("$.signed")
          .asBoolean()
          .isTrue();
    }

    @Test
    @DisplayName("Should return 401 on missing tenant")
    void shouldReturnUnauthorizedOnMissingTenant() {
      mockMvcTester
          .post()
          .uri("/login")
          .contentType(MediaType.APPLICATION_FORM_URLENCODED)
          .param("email", EMAIL)
          .param("password", PASSWORD)
          .exchange()
          .assertThat()
          .hasStatus(HttpStatus.UNAUTHORIZED)
          .bodyJson()
          .extractingPath("$.signed")
          .asBoolean()
          .isFalse();
    }

    @Test
    @DisplayName("Should return 401 on unknown tenant slug")
    void shouldReturnUnauthorizedOnUnknownTenant() {
      mockMvcTester
          .post()
          .uri("/login")
          .contentType(MediaType.APPLICATION_FORM_URLENCODED)
          .param("email", EMAIL)
          .param("password", PASSWORD)
          .param("tenant", "does-not-exist")
          .exchange()
          .assertThat()
          .hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Should return 401 with signed=false on wrong password")
    void shouldReturnUnauthorizedOnWrongPassword() {
      mockMvcTester
          .post()
          .uri("/login")
          .contentType(MediaType.APPLICATION_FORM_URLENCODED)
          .param("email", EMAIL)
          .param("password", "wrong-password")
          .param("tenant", TEST_TENANT_SLUG)
          .exchange()
          .assertThat()
          .hasStatus(HttpStatus.UNAUTHORIZED)
          .bodyJson()
          .extractingPath("$.signed")
          .asBoolean()
          .isFalse();
    }

    @Test
    @DisplayName("Should return 401 on unknown user")
    void shouldReturnUnauthorizedOnUnknownUser() {
      mockMvcTester
          .post()
          .uri("/login")
          .contentType(MediaType.APPLICATION_FORM_URLENCODED)
          .param("email", "ghost@example.com")
          .param("password", PASSWORD)
          .param("tenant", TEST_TENANT_SLUG)
          .exchange()
          .assertThat()
          .hasStatus(HttpStatus.UNAUTHORIZED)
          .bodyJson()
          .extractingPath("$.signed")
          .asBoolean()
          .isFalse();
    }

    @Test
    @DisplayName("Should use 'email' parameter instead of default 'username'")
    void shouldUseEmailParameterInsteadOfUsername() {
      mockMvcTester
          .post()
          .uri("/login")
          .contentType(MediaType.APPLICATION_FORM_URLENCODED)
          .param("username", EMAIL)
          .param("password", PASSWORD)
          .param("tenant", TEST_TENANT_SLUG)
          .exchange()
          .assertThat()
          .hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Should succeed without CSRF token (CSRF disabled)")
    void shouldSucceedWithoutCsrfToken() {
      mockMvcTester
          .post()
          .uri("/login")
          .contentType(MediaType.APPLICATION_FORM_URLENCODED)
          .param("email", EMAIL)
          .param("password", PASSWORD)
          .param("tenant", TEST_TENANT_SLUG)
          .exchange()
          .assertThat()
          .hasStatusOk();
    }
  }
}
