package com.lprevidente.orgcraft.organization;

import static org.assertj.core.api.Assertions.assertThat;

import com.lprevidente.orgcraft.BaseIntegrationTest;
import com.lprevidente.orgcraft.office.domain.OfficeAssignments;
import com.lprevidente.orgcraft.office.domain.Offices;
import com.lprevidente.orgcraft.organization.api.OrganizationId;
import com.lprevidente.orgcraft.organization.domain.Organizations;
import com.lprevidente.orgcraft.team.domain.TeamMembers;
import com.lprevidente.orgcraft.team.domain.Teams;
import com.lprevidente.orgcraft.user.api.UserApi;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

@WithMockUser
@Sql(
    value = {
      "/organizations.sql",
      "/users.sql",
      "/team.sql",
      "/team_members.sql",
      "/offices.sql",
      "/office_assignments.sql"
    },
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class OrganizationDeletionIntegrationTest extends BaseIntegrationTest {

  private static final UUID MARIO_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

  @Autowired private Organizations organizations;
  @Autowired private Teams teams;
  @Autowired private TeamMembers teamMembers;
  @Autowired private Offices offices;
  @Autowired private OfficeAssignments officeAssignments;
  @Autowired private UserApi users;

  @Test
  @DisplayName("Deleting an organization cascade-deletes its teams, offices, members and users")
  void shouldCascadeDeleteEverythingUnderTheOrganization() {
    // Fixtures populate the test tenant (== the organization being deleted).
    assertThat(teams.findAll()).isNotEmpty();
    assertThat(offices.findAll()).isNotEmpty();
    assertThat(users.existsById(MARIO_ID)).isTrue();

    mockMvcTester
        .delete()
        .uri("/api/v1/organizations/{id}", TEST_TENANT)
        .exchange()
        .assertThat()
        .hasStatus(HttpStatus.NO_CONTENT);

    assertThat(organizations.findById(new OrganizationId(UUID.fromString(TEST_TENANT)))).isEmpty();
    assertThat(teams.findAll()).isEmpty();
    assertThat(teamMembers.findAll()).isEmpty();
    assertThat(offices.findAll()).isEmpty();
    assertThat(officeAssignments.findAll()).isEmpty();
    assertThat(users.existsById(MARIO_ID)).isFalse();
  }
}
