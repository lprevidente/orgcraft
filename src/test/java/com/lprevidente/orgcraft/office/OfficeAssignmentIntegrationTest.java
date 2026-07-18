package com.lprevidente.orgcraft.office;

import com.lprevidente.orgcraft.BaseIntegrationTest;
import com.lprevidente.orgcraft.office.application.command.AssignUserToOffice;
import com.lprevidente.orgcraft.office.application.command.RemoveUserFromOffice;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;

@WithUserDetails(value = "mario.rossi@example.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
@Sql(
    value = {"/offices.sql", "/users.sql", "/office_assignments.sql"},
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class OfficeAssignmentIntegrationTest extends BaseIntegrationTest {

  private static final UUID ROME_OFFICE_ID = UUID.fromString("77777777-7777-7777-7777-777777777777");
  private static final UUID MILAN_OFFICE_ID = UUID.fromString("88888888-8888-8888-8888-888888888888");
  private static final UUID LONDON_OFFICE_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
  private static final UUID NON_EXISTENT_OFFICE_ID = UUID.fromString("99999999-9999-9999-9999-999999999999");

  private static final UUID MARIO_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID ANNA_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final UUID GIUSEPPE_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
  private static final UUID NON_EXISTENT_USER_ID = UUID.fromString("99999999-9999-9999-9999-000000000000");

  // Initial state:
  // Rome Office  → Mario (active), Anna (active)
  // Milan Office → Anna (closed 2024-01-15)
  // London Office → empty
  // Giuseppe → unassigned

  @Nested
  @DisplayName("GET /api/v1/offices/{officeId}/members")
  class GetCurrentMembersTest {

    @Test
    @DisplayName("Should return all active members of an office")
    void shouldReturnActiveMembersForOffice() {
      mockMvcTester
          .get()
          .uri("/api/v1/offices/{officeId}/members", ROME_OFFICE_ID)
          .exchange()
          .assertThat()
          .hasStatusOk()
          .bodyJson()
          .extractingPath("$")
          .asArray()
          .hasSize(2);
    }

    @Test
    @DisplayName("Should return empty array for office with no active members")
    void shouldReturnEmptyForOfficeWithNoActiveMembers() {
      mockMvcTester
          .get()
          .uri("/api/v1/offices/{officeId}/members", MILAN_OFFICE_ID)
          .exchange()
          .assertThat()
          .hasStatusOk()
          .bodyJson()
          .extractingPath("$")
          .asArray()
          .isEmpty();
    }

    @Test
    @DisplayName("Should return empty array for non-existent office")
    void shouldReturnEmptyForNonExistentOffice() {
      mockMvcTester
          .get()
          .uri("/api/v1/offices/{officeId}/members", NON_EXISTENT_OFFICE_ID)
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
  @DisplayName("POST /api/v1/offices/{officeId}/members")
  class AssignUserTest {

    @Test
    @DisplayName("Should assign user to office when input is valid")
    void shouldAssignUserToOfficeWhenInputIsValid() throws Exception {
      final var command = new AssignUserToOffice(LONDON_OFFICE_ID, GIUSEPPE_ID);

      mockMvcTester
          .post()
          .uri("/api/v1/offices/{officeId}/members", LONDON_OFFICE_ID)
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonMapper.writeValueAsString(command))
          .exchange()
          .assertThat()
          .hasStatus(HttpStatus.CREATED)
          .bodyJson()
          .extractingPath("$.id")
          .isNotNull();

      mockMvcTester
          .get()
          .uri("/api/v1/offices/{officeId}/members", LONDON_OFFICE_ID)
          .exchange()
          .assertThat()
          .hasStatusOk()
          .bodyJson()
          .extractingPath("$")
          .asArray()
          .hasSize(1);
    }

    @Test
    @DisplayName("Should auto-close previous assignment and reassign to new office")
    void shouldAutoCloseAndReassignToNewOffice() throws Exception {
      final var command = new AssignUserToOffice(MILAN_OFFICE_ID, MARIO_ID);

      mockMvcTester
          .post()
          .uri("/api/v1/offices/{officeId}/members", MILAN_OFFICE_ID)
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonMapper.writeValueAsString(command))
          .exchange()
          .assertThat()
          .hasStatus(HttpStatus.CREATED);

      mockMvcTester
          .get()
          .uri("/api/v1/offices/{officeId}/members", MILAN_OFFICE_ID)
          .exchange()
          .assertThat()
          .hasStatusOk()
          .bodyJson()
          .extractingPath("$")
          .asArray()
          .hasSize(1);

      mockMvcTester
          .get()
          .uri("/api/v1/offices/{officeId}/members", ROME_OFFICE_ID)
          .exchange()
          .assertThat()
          .hasStatusOk()
          .bodyJson()
          .extractingPath("$")
          .asArray()
          .hasSize(1);
    }

    @Test
    @DisplayName("Should return 404 when office does not exist")
    void shouldReturnNotFoundWhenOfficeDoesNotExist() throws Exception {
      final var command = new AssignUserToOffice(NON_EXISTENT_OFFICE_ID, GIUSEPPE_ID);

      mockMvcTester
          .post()
          .uri("/api/v1/offices/{officeId}/members", NON_EXISTENT_OFFICE_ID)
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonMapper.writeValueAsString(command))
          .exchange()
          .assertThat()
          .hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Should return bad request when user does not exist")
    void shouldReturnBadRequestWhenUserDoesNotExist() throws Exception {
      final var command = new AssignUserToOffice(ROME_OFFICE_ID, NON_EXISTENT_USER_ID);

      mockMvcTester
          .post()
          .uri("/api/v1/offices/{officeId}/members", ROME_OFFICE_ID)
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonMapper.writeValueAsString(command))
          .exchange()
          .assertThat()
          .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should return bad request when officeId is null")
    void shouldReturnBadRequestWhenOfficeIdIsNull() {
      mockMvcTester
          .post()
          .uri("/api/v1/offices/{officeId}/members", ROME_OFFICE_ID)
          .contentType(MediaType.APPLICATION_JSON)
          .content("{\"officeId\":null,\"userId\":\"" + GIUSEPPE_ID + "\"}")
          .exchange()
          .assertThat()
          .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should return bad request when userId is null")
    void shouldReturnBadRequestWhenUserIdIsNull() {
      mockMvcTester
          .post()
          .uri("/api/v1/offices/{officeId}/members", ROME_OFFICE_ID)
          .contentType(MediaType.APPLICATION_JSON)
          .content("{\"officeId\":\"" + ROME_OFFICE_ID + "\",\"userId\":null}")
          .exchange()
          .assertThat()
          .hasStatus(HttpStatus.BAD_REQUEST);
    }
  }

  @Nested
  @DisplayName("DELETE /api/v1/offices/{officeId}/members/{userId}")
  class RemoveUserTest {

    @Test
    @DisplayName("Should unassign user from office when assignment is active")
    void shouldUnassignUserFromOfficeWhenAssignmentIsActive() {
      mockMvcTester
          .delete()
          .uri("/api/v1/offices/{officeId}/members/{userId}", ROME_OFFICE_ID, ANNA_ID)
          .exchange()
          .assertThat()
          .hasStatus(HttpStatus.NO_CONTENT);

      mockMvcTester
          .get()
          .uri("/api/v1/offices/{officeId}/members", ROME_OFFICE_ID)
          .exchange()
          .assertThat()
          .hasStatusOk()
          .bodyJson()
          .extractingPath("$")
          .asArray()
          .hasSize(1);
    }

    @Test
    @DisplayName("Should return 404 when user has no active assignment in that office")
    void shouldReturnNotFoundWhenNoActiveAssignment() {
      mockMvcTester
          .delete()
          .uri("/api/v1/offices/{officeId}/members/{userId}", MILAN_OFFICE_ID, ANNA_ID)
          .exchange()
          .assertThat()
          .hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Should return 404 when office does not exist")
    void shouldReturnNotFoundWhenOfficeDoesNotExist() {
      mockMvcTester
          .delete()
          .uri("/api/v1/offices/{officeId}/members/{userId}", NON_EXISTENT_OFFICE_ID, MARIO_ID)
          .exchange()
          .assertThat()
          .hasStatus(HttpStatus.NOT_FOUND);
    }
  }

  @Nested
  @DisplayName("GET /api/v1/users/{userId}/office-history")
  class GetAssignmentHistoryTest {

    @Test
    @DisplayName("Should return full assignment history for a user")
    void shouldReturnAssignmentHistoryForUser() {
      mockMvcTester
          .get()
          .uri("/api/v1/users/{userId}/office-history", ANNA_ID)
          .exchange()
          .assertThat()
          .hasStatusOk()
          .bodyJson()
          .extractingPath("$")
          .asArray()
          .hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("Should return empty array for user with no history")
    void shouldReturnEmptyForUserWithNoHistory() {
      mockMvcTester
          .get()
          .uri("/api/v1/users/{userId}/office-history", NON_EXISTENT_USER_ID)
          .exchange()
          .assertThat()
          .hasStatusOk()
          .bodyJson()
          .extractingPath("$")
          .asArray()
          .isEmpty();
    }
  }
}
