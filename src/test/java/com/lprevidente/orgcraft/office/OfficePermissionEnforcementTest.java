package com.lprevidente.orgcraft.office;

import com.lprevidente.orgcraft.BaseIntegrationTest;
import com.lprevidente.orgcraft.common.authorization.ResourceAuthorization;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;

/**
 * Proves the {@code @PreAuthorize("hasPermission(...)")} guards are actually enforced (i.e. the
 * method-security advice applies to the package-private handler methods). The {@link
 * ResourceAuthorization} mock denies by default (boolean → {@code false}), so a real check would be
 * required to pass — and here it isn't granted, so both endpoints must answer 403.
 */
@WithUserDetails(value = "mario.rossi@example.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
@Sql(
    value = {"/users.sql", "/offices.sql"},
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class OfficePermissionEnforcementTest extends BaseIntegrationTest {

  private static final UUID EXISTING_OFFICE_ID =
      UUID.fromString("77777777-7777-7777-7777-777777777777");

  @MockitoBean private ResourceAuthorization authorization;

  @Test
  @DisplayName("GET /offices/{id} → 403 when the user lacks 'view'")
  void getOfficeForbiddenWithoutView() {
    mockMvcTester
        .get()
        .uri("/api/v1/offices/{id}", EXISTING_OFFICE_ID)
        .exchange()
        .assertThat()
        .hasStatus(HttpStatus.FORBIDDEN);
  }

  @Test
  @DisplayName("DELETE /offices/{id} → 403 when the user lacks 'manage'")
  void deleteOfficeForbiddenWithoutManage() {
    mockMvcTester
        .delete()
        .uri("/api/v1/offices/{id}", EXISTING_OFFICE_ID)
        .exchange()
        .assertThat()
        .hasStatus(HttpStatus.FORBIDDEN);
  }
}
