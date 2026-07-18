package com.lprevidente.orgcraft.office;

import static org.assertj.core.api.Assertions.assertThat;

import com.lprevidente.orgcraft.BaseIntegrationTest;
import com.lprevidente.orgcraft.office.api.OfficeId;
import com.lprevidente.orgcraft.office.domain.OfficeAssignments;
import com.lprevidente.orgcraft.user.api.UserId;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;

@WithUserDetails(value = "mario.rossi@example.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
@Sql(
    value = {"/offices.sql", "/users.sql", "/office_assignments.sql"},
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class OfficeAdminIntegrationTest extends BaseIntegrationTest {

  private static final UUID ROME_OFFICE_ID = UUID.fromString("77777777-7777-7777-7777-777777777777");
  private static final UUID MARIO_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID ANNA_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final UUID GIUSEPPE_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

  @Autowired private OfficeAssignments officeAssignments;

  private boolean isAdmin(UUID officeId, UUID userId) {
    return officeAssignments
        .findByOfficeIdAndUserIdAndUnassignedAtIsNull(new OfficeId(officeId), new UserId(userId))
        .orElseThrow()
        .isAdmin();
  }

  @Test
  @DisplayName("An office can have multiple admins, and admin can be revoked independently")
  void shouldPromoteAndRevokeAdmins() {
    // Mario and Anna are both active occupants of the Rome office.
    promoteAdmin(ROME_OFFICE_ID, MARIO_ID).assertThat().hasStatus(HttpStatus.NO_CONTENT);
    promoteAdmin(ROME_OFFICE_ID, ANNA_ID).assertThat().hasStatus(HttpStatus.NO_CONTENT);

    assertThat(isAdmin(ROME_OFFICE_ID, MARIO_ID)).isTrue();
    assertThat(isAdmin(ROME_OFFICE_ID, ANNA_ID)).isTrue();

    revokeAdmin(ROME_OFFICE_ID, MARIO_ID).assertThat().hasStatus(HttpStatus.NO_CONTENT);

    assertThat(isAdmin(ROME_OFFICE_ID, MARIO_ID)).isFalse();
    assertThat(isAdmin(ROME_OFFICE_ID, ANNA_ID)).isTrue();
  }

  @Test
  @DisplayName("Promoting a user with no active assignment in the office returns 404")
  void shouldReturnNotFoundWhenPromotingNonOccupant() {
    promoteAdmin(ROME_OFFICE_ID, GIUSEPPE_ID).assertThat().hasStatus(HttpStatus.NOT_FOUND);
  }

  private org.springframework.test.web.servlet.assertj.MvcTestResult promoteAdmin(
      UUID officeId, UUID userId) {
    return mockMvcTester.put().uri("/api/v1/offices/{officeId}/members/{userId}/admin", officeId, userId).exchange();
  }

  private org.springframework.test.web.servlet.assertj.MvcTestResult revokeAdmin(
      UUID officeId, UUID userId) {
    return mockMvcTester.delete().uri("/api/v1/offices/{officeId}/members/{userId}/admin", officeId, userId).exchange();
  }
}
