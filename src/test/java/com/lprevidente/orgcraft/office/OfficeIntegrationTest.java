package com.lprevidente.orgcraft.office;

import com.lprevidente.orgcraft.BaseIntegrationTest;
import com.lprevidente.orgcraft.office.application.command.CreateOffice;
import com.lprevidente.orgcraft.office.domain.Address;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;

// Create-office now reads @AuthenticationPrincipal(expression = "id"); authenticate as a real user
// (resolved at TEST_EXECUTION so the tenant context is set first).
@WithUserDetails(value = "mario.rossi@example.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
@Sql(
    value = {"/users.sql", "/offices.sql"},
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class OfficeIntegrationTest extends BaseIntegrationTest {

  private static final UUID EXISTING_OFFICE_ID = UUID.fromString("77777777-7777-7777-7777-777777777777");
  private static final UUID NON_EXISTENT_OFFICE_ID = UUID.fromString("99999999-9999-9999-9999-999999999999");

  @Nested
  @DisplayName("GET /api/v1/offices")
  class GetOfficesTest {

    @Test
    @DisplayName("Should return all offices")
    void shouldReturnAllOffices() {
      mockMvcTester
          .get()
          .uri("/api/v1/offices")
          .exchange()
          .assertThat()
          .hasStatusOk()
          .bodyJson()
          .extractingPath("$[*].name")
          .asArray()
          .hasSizeGreaterThanOrEqualTo(3)
          .contains("Rome Office", "Milan Office", "London Office");
    }
  }

  @Nested
  @DisplayName("GET /api/v1/offices/{id}")
  class GetOfficeByIdTest {

    @Test
    @DisplayName("Should return office when it exists")
    void shouldReturnOfficeWhenItExists() {
      mockMvcTester
          .get()
          .uri("/api/v1/offices/{id}", EXISTING_OFFICE_ID)
          .exchange()
          .assertThat()
          .hasStatusOk()
          .bodyJson()
          .extractingPath("$.name")
          .asString()
          .isEqualTo("Rome Office");
    }

    @Test
    @DisplayName("Should return 404 when office does not exist")
    void shouldReturnNotFoundWhenOfficeDoesNotExist() {
      mockMvcTester
          .get()
          .uri("/api/v1/offices/{id}", NON_EXISTENT_OFFICE_ID)
          .exchange()
          .assertThat()
          .hasStatus(HttpStatus.NOT_FOUND);
    }
  }

  @Nested
  @DisplayName("POST /api/v1/offices")
  class CreateOfficeTest {

    @Test
    @DisplayName("Should create office when input is valid")
    void shouldCreateOfficeWhenInputIsValid() throws Exception {
      final var command =
          new CreateOffice("Berlin Office", new Address("Unter den Linden 1", "Berlin", "Germany"));

      mockMvcTester
          .post()
          .uri("/api/v1/offices")
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
          .uri("/api/v1/offices")
          .exchange()
          .assertThat()
          .hasStatusOk()
          .bodyJson()
          .extractingPath("$[*].name")
          .asArray()
          .contains("Berlin Office");
    }

    @Test
    @DisplayName("Should return bad request when name is blank")
    void shouldReturnBadRequestWhenNameIsBlank() throws Exception {
      final var command =
          new CreateOffice("", new Address("Unter den Linden 1", "Berlin", "Germany"));

      mockMvcTester
          .post()
          .uri("/api/v1/offices")
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonMapper.writeValueAsString(command))
          .exchange()
          .assertThat()
          .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should return bad request when name is null")
    void shouldReturnBadRequestWhenNameIsNull() {
      mockMvcTester
          .post()
          .uri("/api/v1/offices")
          .contentType(MediaType.APPLICATION_JSON)
          .content(
              "{\"name\":null,\"address\":{\"address\":\"Via Roma 1\",\"city\":\"Rome\",\"country\":\"Italy\"}}")
          .exchange()
          .assertThat()
          .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should return bad request when address is null")
    void shouldReturnBadRequestWhenAddressIsNull() {
      mockMvcTester
          .post()
          .uri("/api/v1/offices")
          .contentType(MediaType.APPLICATION_JSON)
          .content("{\"name\":\"Test Office\",\"address\":null}")
          .exchange()
          .assertThat()
          .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should return bad request when JSON is malformed")
    void shouldReturnBadRequestWhenJsonIsMalformed() {
      mockMvcTester
          .post()
          .uri("/api/v1/offices")
          .contentType(MediaType.APPLICATION_JSON)
          .content("{malformed json}")
          .exchange()
          .assertThat()
          .hasStatus(HttpStatus.BAD_REQUEST);
    }
  }

  @Nested
  @DisplayName("DELETE /api/v1/offices/{id}")
  class DeleteOfficeTest {

    @Test
    @DisplayName("Should delete office when it exists")
    void shouldDeleteOfficeWhenItExists() {
      mockMvcTester
          .delete()
          .uri("/api/v1/offices/{id}", EXISTING_OFFICE_ID)
          .exchange()
          .assertThat()
          .hasStatus(HttpStatus.NO_CONTENT);

      mockMvcTester
          .get()
          .uri("/api/v1/offices")
          .exchange()
          .assertThat()
          .hasStatusOk()
          .bodyJson()
          .extractingPath("$[*].name")
          .asArray()
          .doesNotContain("Rome Office");
    }

    @Test
    @DisplayName("Should return 404 when office does not exist")
    void shouldReturnNotFoundWhenOfficeDoesNotExist() {
      mockMvcTester
          .delete()
          .uri("/api/v1/offices/{id}", NON_EXISTENT_OFFICE_ID)
          .exchange()
          .assertThat()
          .hasStatus(HttpStatus.NOT_FOUND);
    }
  }
}
