package com.lprevidente.orgcraft.team;

import static org.assertj.core.api.Assertions.assertThat;

import com.lprevidente.orgcraft.BaseIntegrationTest;
import com.lprevidente.orgcraft.team.api.TeamId;
import com.lprevidente.orgcraft.team.domain.TeamMemberId;
import com.lprevidente.orgcraft.team.domain.TeamMembers;
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
    value = {"/users.sql", "/team.sql", "/team_members.sql"},
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class TeamAdminIntegrationTest extends BaseIntegrationTest {

  private static final UUID DEV_TEAM_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
  private static final UUID MARIO_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID ANNA_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final UUID GIUSEPPE_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

  @Autowired private TeamMembers teamMembers;

  private boolean isAdmin(UUID teamId, UUID userId) {
    return teamMembers
        .findById(new TeamMemberId(new TeamId(teamId), new UserId(userId)))
        .orElseThrow()
        .isAdmin();
  }

  @Test
  @DisplayName("A team can have multiple admins, and admin can be revoked independently")
  void shouldPromoteAndRevokeAdmins() {
    // Mario and Anna are both members of the Development team.
    promoteAdmin(DEV_TEAM_ID, MARIO_ID).assertThat().hasStatus(HttpStatus.NO_CONTENT);
    promoteAdmin(DEV_TEAM_ID, ANNA_ID).assertThat().hasStatus(HttpStatus.NO_CONTENT);

    assertThat(isAdmin(DEV_TEAM_ID, MARIO_ID)).isTrue();
    assertThat(isAdmin(DEV_TEAM_ID, ANNA_ID)).isTrue();

    revokeAdmin(DEV_TEAM_ID, MARIO_ID).assertThat().hasStatus(HttpStatus.NO_CONTENT);

    assertThat(isAdmin(DEV_TEAM_ID, MARIO_ID)).isFalse();
    assertThat(isAdmin(DEV_TEAM_ID, ANNA_ID)).isTrue();
  }

  @Test
  @DisplayName("Promoting a user who is not a member of the team returns 404")
  void shouldReturnNotFoundWhenPromotingNonMember() {
    // Giuseppe is a member of Marketing, not Development.
    promoteAdmin(DEV_TEAM_ID, GIUSEPPE_ID).assertThat().hasStatus(HttpStatus.NOT_FOUND);
  }

  private org.springframework.test.web.servlet.assertj.MvcTestResult promoteAdmin(
      UUID teamId, UUID userId) {
    return mockMvcTester.put().uri("/api/teams/{teamId}/members/{userId}/admin", teamId, userId).exchange();
  }

  private org.springframework.test.web.servlet.assertj.MvcTestResult revokeAdmin(
      UUID teamId, UUID userId) {
    return mockMvcTester.delete().uri("/api/teams/{teamId}/members/{userId}/admin", teamId, userId).exchange();
  }
}
