package com.lprevidente.orgcraft.team;

import static org.assertj.core.api.Assertions.assertThat;

import com.lprevidente.orgcraft.BaseIntegrationTest;
import com.lprevidente.orgcraft.team.application.command.AddUserToTeam;
import com.lprevidente.orgcraft.team.domain.event.RemovedUserFromTeam;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.context.jdbc.Sql;

@RecordApplicationEvents
@WithMockUser
@Sql(
    value = {"/users.sql", "/team.sql", "/team_members.sql"},
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class TeamMemberIntegrationTest extends BaseIntegrationTest {

  private static final UUID EXISTING_TEAM_ID_UUID = UUID.fromString("44444444-4444-4444-4444-444444444444");
  private static final UUID SALES_TEAM_ID_UUID = UUID.fromString("66666666-6666-6666-6666-666666666666");
  private static final UUID NON_EXISTENT_TEAM_ID_UUID = UUID.fromString("99999999-9999-9999-9999-999999999999");
  private static final UUID EXISTING_USER_ID_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID NEW_USER_ID_UUID = UUID.fromString("33333333-3333-3333-3333-333333333333");
  private static final UUID NON_EXISTENT_USER_ID_UUID = UUID.fromString("88888888-8888-8888-8888-888888888888");

  @Nested
  @DisplayName("GET /api/teams/{id}/members")
  class GetTeamMembersTest {

    @Test
    @DisplayName("Should return all members of a team")
    void shouldReturnAllMembersOfTeam() {
      mockMvcTester
          .get()
          .uri("/api/teams/{teamId}/members", EXISTING_TEAM_ID_UUID)
          .exchange()
          .assertThat()
          .hasStatusOk()
          .bodyJson()
          .extractingPath("$")
          .asArray()
          .hasSize(2);
    }

    @Test
    @DisplayName("Should return empty array for team with no members")
    void shouldReturnEmptyArrayForTeamWithNoMembers() {
      mockMvcTester
          .get()
          .uri("/api/teams/{teamId}/members", SALES_TEAM_ID_UUID)
          .exchange()
          .assertThat()
          .hasStatusOk()
          .bodyJson()
          .extractingPath("$")
          .asArray()
          .isEmpty();
    }

    @Test
    @DisplayName("Should return empty array for non-existent team")
    void shouldReturnEmptyArrayForNonExistentTeam() {
      mockMvcTester
          .get()
          .uri("/api/teams/{teamId}/members", NON_EXISTENT_TEAM_ID_UUID)
          .exchange()
          .assertThat()
          .hasStatusOk()
          .bodyJson()
          .extractingPath("$")
          .asArray()
          .isEmpty();
    }
  }

  @Nested
  @DisplayName("POST /api/teams/{id}/members")
  class AddTeamMemberTest {

    @Test
    @DisplayName("Should add user to team when input is valid")
    void shouldAddUserToTeamWhenInputIsValid() throws Exception {
      final var addUserToTeam = new AddUserToTeam(SALES_TEAM_ID_UUID, NEW_USER_ID_UUID);

      mockMvcTester
          .post()
          .uri("/api/teams/{teamId}/members", SALES_TEAM_ID_UUID)
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonMapper.writeValueAsString(addUserToTeam))
          .exchange()
          .assertThat()
          .hasStatus(HttpStatus.CREATED);

      mockMvcTester
          .get()
          .uri("/api/teams/{teamId}/members", SALES_TEAM_ID_UUID)
          .exchange()
          .assertThat()
          .hasStatusOk()
          .bodyJson()
          .extractingPath("$")
          .asArray()
          .hasSize(1);
    }

    @Test
    @DisplayName("Should return bad request when user is already a member")
    void shouldReturnBadRequestWhenUserIsAlreadyMember() throws Exception {
      final var addUserToTeam = new AddUserToTeam(EXISTING_TEAM_ID_UUID, EXISTING_USER_ID_UUID);

      mockMvcTester
          .post()
          .uri("/api/teams/{teamId}/members", EXISTING_TEAM_ID_UUID)
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonMapper.writeValueAsString(addUserToTeam))
          .exchange()
          .assertThat()
          .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should return bad request when user ID is null")
    void shouldReturnBadRequestWhenUserIdIsNull() {
      mockMvcTester
          .post()
          .uri("/api/teams/{teamId}/members", EXISTING_TEAM_ID_UUID)
          .contentType(MediaType.APPLICATION_JSON)
          .content("{\"id\":\"" + EXISTING_TEAM_ID_UUID + "\",\"userId\":null}")
          .exchange()
          .assertThat()
          .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should return bad request when team ID is null")
    void shouldReturnBadRequestWhenTeamIdIsNull() {
      mockMvcTester
          .post()
          .uri("/api/teams/{teamId}/members", EXISTING_TEAM_ID_UUID)
          .contentType(MediaType.APPLICATION_JSON)
          .content("{\"id\":null,\"userId\":\"" + NEW_USER_ID_UUID + "\"}")
          .exchange()
          .assertThat()
          .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should return bad request when JSON is malformed")
    void shouldReturnBadRequestWhenJsonIsMalformed() {
      mockMvcTester
          .post()
          .uri("/api/teams/{teamId}/members", EXISTING_TEAM_ID_UUID)
          .contentType(MediaType.APPLICATION_JSON)
          .content("{malformed json}")
          .exchange()
          .assertThat()
          .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should return bad request when user does not exist")
    void shouldReturnBadRequestWhenUserDoesNotExist() throws Exception {
      final var addUserToTeam = new AddUserToTeam(EXISTING_TEAM_ID_UUID, NON_EXISTENT_USER_ID_UUID);

      mockMvcTester
          .post()
          .uri("/api/teams/{teamId}/members", EXISTING_TEAM_ID_UUID)
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonMapper.writeValueAsString(addUserToTeam))
          .exchange()
          .assertThat()
          .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should return bad request when team does not exist")
    void shouldReturnBadRequestWhenTeamDoesNotExist() throws Exception {
      final var addUserToTeam = new AddUserToTeam(NON_EXISTENT_TEAM_ID_UUID, NEW_USER_ID_UUID);

      mockMvcTester
          .post()
          .uri("/api/teams/{teamId}/members", NON_EXISTENT_TEAM_ID_UUID)
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonMapper.writeValueAsString(addUserToTeam))
          .exchange()
          .assertThat()
          .hasStatus(HttpStatus.BAD_REQUEST);
    }
  }

  @Nested
  @DisplayName("DELETE /api/teams/{id}/members/{userId}")
  class RemoveTeamMemberTest {

    @Test
    @DisplayName("Should remove user from team when user is a member")
    void shouldRemoveUserFromTeamWhenUserIsMember(ApplicationEvents events) {
      mockMvcTester
          .delete()
          .uri("/api/teams/{teamId}/members/{userId}", EXISTING_TEAM_ID_UUID, EXISTING_USER_ID_UUID)
          .exchange()
          .assertThat()
          .hasStatus(HttpStatus.NO_CONTENT);

      mockMvcTester
          .get()
          .uri("/api/teams/{teamId}/members", EXISTING_TEAM_ID_UUID)
          .exchange()
          .assertThat()
          .hasStatusOk()
          .bodyJson()
          .extractingPath("$")
          .asArray()
          .hasSize(1);

      assertThat(events.stream(RemovedUserFromTeam.class))
          .singleElement()
          .satisfies(
              e -> {
                assertThat(e.teamId().id()).isEqualTo(EXISTING_TEAM_ID_UUID);
                assertThat(e.userId().id()).isEqualTo(EXISTING_USER_ID_UUID);
              });
    }

    @Test
    @DisplayName("Should return not found when user is not a member")
    void shouldReturnNotFoundWhenUserIsNotMember() {
      mockMvcTester
          .delete()
          .uri("/api/teams/{teamId}/members/{userId}", SALES_TEAM_ID_UUID, EXISTING_USER_ID_UUID)
          .exchange()
          .assertThat()
          .hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Should return not found when team does not exist")
    void shouldReturnNotFoundWhenTeamDoesNotExist() {
      mockMvcTester
          .delete()
          .uri(
              "/api/teams/{teamId}/members/{userId}",
              NON_EXISTENT_TEAM_ID_UUID,
              EXISTING_USER_ID_UUID)
          .exchange()
          .assertThat()
          .hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Should return not found when user does not exist")
    void shouldReturnNotFoundWhenUserDoesNotExist() {
      mockMvcTester
          .delete()
          .uri(
              "/api/teams/{teamId}/members/{userId}",
              EXISTING_TEAM_ID_UUID,
              NON_EXISTENT_USER_ID_UUID)
          .exchange()
          .assertThat()
          .hasStatus(HttpStatus.NOT_FOUND);
    }
  }
}
