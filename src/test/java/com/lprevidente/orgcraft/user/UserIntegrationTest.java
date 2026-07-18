package com.lprevidente.orgcraft.user;

import com.lprevidente.orgcraft.BaseIntegrationTest;
import com.lprevidente.orgcraft.user.application.command.CreateUserReq;
import com.lprevidente.orgcraft.user.application.command.UpdateUser;
import com.lprevidente.orgcraft.user.api.UserId;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;

// Authenticate as Giuseppe — a user no test mutates — so the principal resolves even after the
// delete test removes Mario (the @Sql fixture loads once for the whole class).
@WithUserDetails(value = "giuseppe.verdi@example.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
@Sql(value = "/users.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class UserIntegrationTest extends BaseIntegrationTest {

  private static final UUID EXISTING_USER_ID_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID NON_EXISTENT_USER_ID_UUID = UUID.fromString("99999999-9999-9999-9999-999999999999");

  private static final UserId EXISTING_USER_ID = new UserId(EXISTING_USER_ID_UUID);
  private static final UserId NON_EXISTENT_USER_ID = new UserId(NON_EXISTENT_USER_ID_UUID);

  @Nested
  @DisplayName("GET /api/v1/users")
  class GetUsersTest {

    @Test
    @DisplayName("Should return all users")
    void shouldReturnAllUsers() {
      mockMvcTester
          .get()
          .uri("/api/v1/users")
          .exchange()
          .assertThat()
          .hasStatusOk()
          .bodyJson()
          .extractingPath("$[*].firstName")
          .asArray()
          .hasSizeGreaterThanOrEqualTo(3)
          .contains("Mario", "Anna", "Giuseppe");
    }
  }

  @Nested
  @DisplayName("GET /api/v1/users/{id}")
  class GetUserByIdTest {

    @Test
    @DisplayName("Should return user when user exists")
    void shouldReturnUserWhenUserExists() {
      mockMvcTester
          .get()
          .uri("/api/v1/users/{id}", EXISTING_USER_ID_UUID)
          .exchange()
          .assertThat()
          .hasStatusOk()
          .bodyJson()
          .extractingPath("$.firstName")
          .asString()
          .isEqualTo("Mario");
    }

    @Test
    @DisplayName("Should return 404 when user does not exist")
    void shouldReturnNotFoundWhenUserDoesNotExist() {
      mockMvcTester
          .get()
          .uri("/api/v1/users/{id}", NON_EXISTENT_USER_ID_UUID)
          .exchange()
          .assertThat()
          .hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Should return bad request when ID is invalid")
    void shouldReturnBadRequestWhenIdIsInvalid() {
      mockMvcTester
          .get()
          .uri("/api/v1/users/invalid-uuid")
          .exchange()
          .assertThat()
          .hasStatus(HttpStatus.BAD_REQUEST);
    }
  }

  @Nested
  @DisplayName("POST /api/v1/users")
  class CreateUserTest {

    @Test
    @DisplayName("Should create user when input is valid")
    void shouldCreateUserWhenInputIsValid() {
      final var addUser = new CreateUserReq("Paolo", "Neri", "paolo.neri@example.com", "Password@123");

      mockMvcTester
          .post()
          .uri("/api/v1/users")
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonMapper.writeValueAsString(addUser))
          .exchange()
          .assertThat()
          .hasStatus(HttpStatus.CREATED)
          .bodyJson()
          .isNotNull();

      mockMvcTester
          .get()
          .uri("/api/v1/users")
          .exchange()
          .assertThat()
          .hasStatusOk()
          .bodyJson()
          .extractingPath("$[*].firstName")
          .asArray()
          .contains("Paolo");
    }

    @Test
    @DisplayName("Should return bad request when firstName is blank")
    void shouldReturnBadRequestWhenFirstNameIsBlank() {
      final var addUser = new CreateUserReq("", "Neri", "paolo.neri@example.com", "Password@123");

      mockMvcTester
          .post()
          .uri("/api/v1/users")
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonMapper.writeValueAsString(addUser))
          .exchange()
          .assertThat()
          .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should return bad request when lastName is blank")
    void shouldReturnBadRequestWhenLastNameIsBlank() {
      final var addUser = new CreateUserReq("Paolo", "", "paolo.neri@example.com", "Password@123");

      mockMvcTester
          .post()
          .uri("/api/v1/users")
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonMapper.writeValueAsString(addUser))
          .exchange()
          .assertThat()
          .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should return bad request when email is blank")
    void shouldReturnBadRequestWhenEmailIsBlank() {
      final var addUser = new CreateUserReq("Paolo", "Neri", "", "Password@123");

      mockMvcTester
          .post()
          .uri("/api/v1/users")
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonMapper.writeValueAsString(addUser))
          .exchange()
          .assertThat()
          .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should return bad request when password is blank")
    void shouldReturnBadRequestWhenPasswordIsBlank() {
      final var addUser = new CreateUserReq("Paolo", "Neri", "paolo.neri@example.com", "");

      mockMvcTester
          .post()
          .uri("/api/v1/users")
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonMapper.writeValueAsString(addUser))
          .exchange()
          .assertThat()
          .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should return bad request when password is too weak")
    void shouldReturnBadRequestWhenPasswordIsTooWeak() {
      final var addUser = new CreateUserReq("Paolo", "Neri", "paolo.neri@example.com", "weak");

      mockMvcTester
          .post()
          .uri("/api/v1/users")
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonMapper.writeValueAsString(addUser))
          .exchange()
          .assertThat()
          .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should return conflict when email already exists")
    void shouldReturnBadRequestWhenEmailAlreadyExists() {
      final var addUser =
          new CreateUserReq("Duplicate", "User", "mario.rossi@example.com", "Password@123");

      mockMvcTester
          .post()
          .uri("/api/v1/users")
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonMapper.writeValueAsString(addUser))
          .exchange()
          .assertThat()
          .hasStatus(HttpStatus.CONFLICT)
          .bodyJson()
          .extractingPath("$.errorCode")
          .asString()
          .isEqualTo("EMAIL_ALREADY_IN_USE");
    }

    @Test
    @DisplayName("Should return bad request when JSON is malformed")
    void shouldReturnBadRequestWhenJsonIsMalformed() {
      mockMvcTester
          .post()
          .uri("/api/v1/users")
          .contentType(MediaType.APPLICATION_JSON)
          .content("{malformed json}")
          .exchange()
          .assertThat()
          .hasStatus(HttpStatus.BAD_REQUEST);
    }
  }

  @Nested
  @DisplayName("PUT /api/v1/users")
  class UpdateUserTest {

    @Test
    @DisplayName("Should update user when input is valid")
    void shouldUpdateUserWhenInputIsValid() {
      final var updateUser =
          new UpdateUser(EXISTING_USER_ID, "Mario Aggiornato", "Rossi Aggiornato");

      mockMvcTester
          .put()
          .uri("/api/v1/users")
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonMapper.writeValueAsString(updateUser))
          .exchange()
          .assertThat()
          .hasStatus(HttpStatus.NO_CONTENT);

      mockMvcTester
          .get()
          .uri("/api/v1/users/{id}", EXISTING_USER_ID_UUID)
          .exchange()
          .assertThat()
          .hasStatusOk()
          .bodyJson()
          .extractingPath("$.firstName")
          .asString()
          .isEqualTo("Mario Aggiornato");
    }

    @Test
    @DisplayName("Should return 404 when user does not exist")
    void shouldReturnNotFoundWhenUserDoesNotExist() {
      final var updateUser = new UpdateUser(NON_EXISTENT_USER_ID, "Nome", "Cognome");

      mockMvcTester
          .put()
          .uri("/api/v1/users")
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonMapper.writeValueAsString(updateUser))
          .exchange()
          .assertThat()
          .hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Should return bad request when firstName is blank")
    void shouldReturnBadRequestWhenFirstNameIsBlank() {
      final var updateUser = new UpdateUser(EXISTING_USER_ID, "", "Rossi");

      mockMvcTester
          .put()
          .uri("/api/v1/users")
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonMapper.writeValueAsString(updateUser))
          .exchange()
          .assertThat()
          .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should return bad request when lastName is blank")
    void shouldReturnBadRequestWhenLastNameIsBlank() {
      final var updateUser = new UpdateUser(EXISTING_USER_ID, "Mario", "");

      mockMvcTester
          .put()
          .uri("/api/v1/users")
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonMapper.writeValueAsString(updateUser))
          .exchange()
          .assertThat()
          .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should return bad request when id is null")
    void shouldReturnBadRequestWhenIdIsNull() {
      mockMvcTester
          .put()
          .uri("/api/v1/users")
          .contentType(MediaType.APPLICATION_JSON)
          .content("{\"id\":null,\"firstName\":\"Mario\",\"lastName\":\"Rossi\"}")
          .exchange()
          .assertThat()
          .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should return bad request when JSON is malformed")
    void shouldReturnBadRequestWhenJsonIsMalformed() {
      mockMvcTester
          .put()
          .uri("/api/v1/users")
          .contentType(MediaType.APPLICATION_JSON)
          .content("{malformed json}")
          .exchange()
          .assertThat()
          .hasStatus(HttpStatus.BAD_REQUEST);
    }
  }

  @Nested
  @DisplayName("DELETE /api/v1/users/{id}")
  class DeleteUserTest {

    @Test
    @DisplayName("Should delete user when user exists")
    void shouldDeleteUserWhenUserExists() {
      mockMvcTester
          .delete()
          .uri("/api/v1/users/{id}", EXISTING_USER_ID_UUID)
          .exchange()
          .assertThat()
          .hasStatus(HttpStatus.NO_CONTENT);

      mockMvcTester
          .get()
          .uri("/api/v1/users/{id}", EXISTING_USER_ID_UUID)
          .exchange()
          .assertThat()
          .hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existent user")
    void shouldReturnNotFoundWhenDeletingNonExistentUser() {
      mockMvcTester
          .delete()
          .uri("/api/v1/users/{id}", NON_EXISTENT_USER_ID_UUID)
          .exchange()
          .assertThat()
          .hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Should return bad request when ID is invalid")
    void shouldReturnBadRequestWhenIdIsInvalid() {
      mockMvcTester
          .delete()
          .uri("/api/v1/users/invalid-uuid")
          .exchange()
          .assertThat()
          .hasStatus(HttpStatus.BAD_REQUEST);
    }
  }
}
