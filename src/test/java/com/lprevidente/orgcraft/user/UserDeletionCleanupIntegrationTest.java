package com.lprevidente.orgcraft.user;

import static org.assertj.core.api.Assertions.assertThat;

import com.lprevidente.orgcraft.BaseIntegrationTest;
import com.lprevidente.orgcraft.office.domain.OfficeAssignments;
import com.lprevidente.orgcraft.office.api.OfficeId;
import com.lprevidente.orgcraft.office.domain.Offices;
import com.lprevidente.orgcraft.team.api.TeamId;
import com.lprevidente.orgcraft.team.domain.TeamMembers;
import com.lprevidente.orgcraft.team.domain.Teams;
import com.lprevidente.orgcraft.user.api.UserId;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

/**
 * Fixtures: Mario ({@code 1111…}) creates all 3 teams and offices, is a Development-team member and
 * is assigned to the Rome office. Anna ({@code 2222…}) shares both and must be left intact.
 */
@WithMockUser
@Sql(
    value = {"/users.sql", "/team.sql", "/team_members.sql", "/offices.sql", "/office_assignments.sql"},
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class UserDeletionCleanupIntegrationTest extends BaseIntegrationTest {

  private static final UUID MARIO_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID ANNA_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

  private static final TeamId DEV_TEAM_ID = new TeamId(UUID.fromString("44444444-4444-4444-4444-444444444444"));
  private static final OfficeId ROME_OFFICE_ID = new OfficeId(UUID.fromString("77777777-7777-7777-7777-777777777777"));

  @Autowired private Teams teams;
  @Autowired private TeamMembers teamMembers;
  @Autowired private Offices offices;
  @Autowired private OfficeAssignments officeAssignments;

  @Test
  @DisplayName("Deleting a user strips memberships/assignments, orphans creators, keeps resources")
  void shouldCleanUpUserRelationsOnDeletion() {
    final var mario = new UserId(MARIO_ID);
    final var anna = new UserId(ANNA_ID);

    assertThat(teams.findByCreator(mario)).hasSize(3);
    assertThat(teamMembers.findByIdUserId(mario)).isNotEmpty();
    assertThat(offices.findByCreator(mario)).hasSize(3);
    assertThat(officeAssignments.findByUserId(mario)).isNotEmpty();

    mockMvcTester
        .delete()
        .uri("/api/v1/users/{id}", MARIO_ID)
        .exchange()
        .assertThat()
        .hasStatus(HttpStatus.NO_CONTENT);

    assertThat(teamMembers.findByIdUserId(mario)).isEmpty();
    assertThat(teamMembers.findByIdUserId(anna)).isNotEmpty();

    assertThat(officeAssignments.findByUserId(mario)).isEmpty();
    assertThat(officeAssignments.findByUserId(anna)).isNotEmpty();

    assertThat(teams.findByCreator(mario)).isEmpty();
    assertThat(offices.findByCreator(mario)).isEmpty();
    assertThat(teams.findById(DEV_TEAM_ID)).hasValueSatisfying(t -> assertThat(t.getCreator()).isNull());
    assertThat(offices.findById(ROME_OFFICE_ID)).hasValueSatisfying(o -> assertThat(o.getCreator()).isNull());
  }
}
