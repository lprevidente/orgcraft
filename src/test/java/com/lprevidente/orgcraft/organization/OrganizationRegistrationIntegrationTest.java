package com.lprevidente.orgcraft.organization;

import static org.assertj.core.api.Assertions.assertThat;

import com.lprevidente.orgcraft.BaseIntegrationTest;
import com.lprevidente.orgcraft.organization.application.command.RegisterOrganization;
import com.lprevidente.orgcraft.organization.application.command.RegisterOrganizationRes;
import com.lprevidente.orgcraft.tenancy.TenantContext;
import com.lprevidente.orgcraft.user.api.UserApi;
import com.lprevidente.orgcraft.user.application.projection.UserView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

@DisplayName("POST /api/v1/organizations")
class OrganizationRegistrationIntegrationTest extends BaseIntegrationTest {

  @Autowired private UserApi userApi;

  @Test
  @DisplayName("Should register organization and founder atomically")
  void shouldRegisterOrganizationAndFounder() {
    final var command =
        new RegisterOrganization(
            "Acme Inc", "acme", "Alice", "Founder", "alice@acme.com", "Password@123");

    final var result =
        mockMvcTester
            .post()
            .uri("/api/v1/organizations")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonMapper.writeValueAsString(command))
            .exchange();

    result.assertThat().hasStatus(HttpStatus.CREATED);

    final var response =
        jsonMapper.readValue(
            result.getResponse().getContentAsByteArray(), RegisterOrganizationRes.class);

    assertThat(response.organizationId()).isNotNull();
    assertThat(response.founderId()).isNotNull();

    TenantContext.set(response.organizationId().id().toString());
    try {
      final var founder = userApi.findByEmail("alice@acme.com", UserView.class);
      assertThat(founder).isPresent();
      assertThat(founder.get().getFirstName()).isEqualTo("Alice");
    } finally {
      TenantContext.set(TEST_TENANT);
    }
  }

  @Test
  @DisplayName("Should reject duplicate slug with 409")
  void shouldRejectDuplicateSlug() {
    final var first =
        new RegisterOrganization(
            "First Org", "duplicate-slug", "First", "Founder", "first@example.com", "Password@123");
    final var second =
        new RegisterOrganization(
            "Second Org",
            "duplicate-slug",
            "Second",
            "Founder",
            "second@example.com",
            "Password@123");

    mockMvcTester
        .post()
        .uri("/api/v1/organizations")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonMapper.writeValueAsString(first))
        .exchange()
        .assertThat()
        .hasStatus(HttpStatus.CREATED);

    mockMvcTester
        .post()
        .uri("/api/v1/organizations")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonMapper.writeValueAsString(second))
        .exchange()
        .assertThat()
        .hasStatus(HttpStatus.CONFLICT)
        .bodyJson()
        .extractingPath("$.errorCode")
        .asString()
        .isEqualTo("SLUG_ALREADY_IN_USE");
  }

  @Test
  @DisplayName("Should reject invalid slug format with 400")
  void shouldRejectInvalidSlug() {
    final var command =
        new RegisterOrganization(
            "Bad Slug Co", "Bad Slug!", "First", "Founder", "bad@example.com", "Password@123");

    mockMvcTester
        .post()
        .uri("/api/v1/organizations")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonMapper.writeValueAsString(command))
        .exchange()
        .assertThat()
        .hasStatus(HttpStatus.BAD_REQUEST);
  }

  @Test
  @DisplayName("Should reject blank name with 400")
  void shouldRejectBlankName() {
    final var command =
        new RegisterOrganization(
            "", "blank-name", "First", "Founder", "blank@example.com", "Password@123");

    mockMvcTester
        .post()
        .uri("/api/v1/organizations")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonMapper.writeValueAsString(command))
        .exchange()
        .assertThat()
        .hasStatus(HttpStatus.BAD_REQUEST);
  }

  @Test
  @DisplayName("Should allow same email across different tenants")
  void shouldAllowSameEmailAcrossTenants() {
    final var first =
        new RegisterOrganization(
            "First Co", "first-co", "First", "Founder", "shared@example.com", "Password@123");
    final var second =
        new RegisterOrganization(
            "Second Co", "second-co", "Second", "Founder", "shared@example.com", "Password@123");

    mockMvcTester
        .post()
        .uri("/api/v1/organizations")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonMapper.writeValueAsString(first))
        .exchange()
        .assertThat()
        .hasStatus(HttpStatus.CREATED);

    mockMvcTester
        .post()
        .uri("/api/v1/organizations")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonMapper.writeValueAsString(second))
        .exchange()
        .assertThat()
        .hasStatus(HttpStatus.CREATED);
  }
}
