package com.lprevidente.orgcraft.team;

import com.lprevidente.orgcraft.BaseIntegrationTest;
import com.lprevidente.orgcraft.team.application.command.CreateTeam;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;

// Resolved at TEST_EXECUTION so the @BeforeEach tenant context is set before the tenant-filtered
// user lookup runs; gives a real UserDetailsView principal whose getId() feeds @AuthenticationPrincipal.
@WithUserDetails(value = "mario.rossi@example.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
@Sql(
    value = {"/users.sql", "/team.sql"},
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class TeamIntegrationTest extends BaseIntegrationTest {

  private static final UUID EXISTING_TEAM_ID_UUID = UUID.fromString("44444444-4444-4444-4444-444444444444");
  private static final UUID NON_EXISTENT_TEAM_ID_UUID = UUID.fromString("99999999-9999-9999-9999-999999999999");

  @Nested
  @DisplayName("GET /api/v1/teams")
  class GetTeamsTest {

    @Test
    @DisplayName("Should return all teams")
    void shouldReturnAllTeams() {
      mockMvcTester
          .get()
          .uri("/api/v1/teams")
          .exchange()
          .assertThat()
          .hasStatusOk()
          .bodyJson()
          .extractingPath("$[*].name")
          .asArray()
          .hasSizeGreaterThanOrEqualTo(3)
          .contains("Development Team", "Marketing Team", "Sales Team");
    }
  }

  @Nested
  @DisplayName("POST /api/v1/teams")
  class CreateTeamTest {

    @Test
    @DisplayName("Should create team when input is valid")
    void shouldCreateTeamWhenInputIsValid() {
      final var createTeam = new CreateTeam("QA Team");

      mockMvcTester
          .post()
          .uri("/api/v1/teams")
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonMapper.writeValueAsString(createTeam))
          .exchange()
          .assertThat()
          .hasStatus(HttpStatus.CREATED)
          .bodyJson()
          .isNotNull();

      mockMvcTester
          .get()
          .uri("/api/v1/teams")
          .exchange()
          .assertThat()
          .hasStatusOk()
          .bodyJson()
          .extractingPath("$[*].name")
          .asArray()
          .contains("QA Team");
    }

    @Test
    @DisplayName("Should return bad request when name is blank")
    void shouldReturnBadRequestWhenNameIsBlank()  {
      final var createTeam = new CreateTeam("");

      mockMvcTester
          .post()
          .uri("/api/v1/teams")
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonMapper.writeValueAsString(createTeam))
          .exchange()
          .assertThat()
          .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should return bad request when name is null")
    void shouldReturnBadRequestWhenNameIsNull() {
      mockMvcTester
          .post()
          .uri("/api/v1/teams")
          .contentType(MediaType.APPLICATION_JSON)
          .content("{\"name\":null}")
          .exchange()
          .assertThat()
          .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should return bad request when JSON is malformed")
    void shouldReturnBadRequestWhenJsonIsMalformed() {
      mockMvcTester
          .post()
          .uri("/api/v1/teams")
          .contentType(MediaType.APPLICATION_JSON)
          .content("{malformed json}")
          .exchange()
          .assertThat()
          .hasStatus(HttpStatus.BAD_REQUEST);
    }
  }

  @Nested
  @DisplayName("DELETE /api/v1/teams")
  class DeleteTeamTest {

    @Test
    @DisplayName("Should delete team when team exists")
    void shouldDeleteTeamWhenTeamExists() {
      mockMvcTester
          .delete()
          .uri("/api/v1/teams/{id}", EXISTING_TEAM_ID_UUID)
          .exchange()
          .assertThat()
          .hasStatus(HttpStatus.NO_CONTENT);

      mockMvcTester
          .get()
          .uri("/api/v1/teams")
          .exchange()
          .assertThat()
          .hasStatusOk()
          .bodyJson()
          .extractingPath("$[*].name")
          .asArray()
          .doesNotContain("Development Team");
    }

    @Test
    @DisplayName("Should return 404 when team does not exist")
    void shouldReturnNotFoundWhenTeamDoesNotExist() {
      mockMvcTester
          .delete()
          .uri("/api/v1/teams/{id}", NON_EXISTENT_TEAM_ID_UUID)
          .exchange()
          .assertThat()
          .hasStatus(HttpStatus.NOT_FOUND);
    }
  }
}
