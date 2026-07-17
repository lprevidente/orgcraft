package com.lprevidente.orgcraft.office.infrastructure.rest;

import com.lprevidente.orgcraft.office.application.command.CreateOffice;
import com.lprevidente.orgcraft.office.application.command.DeleteOffice;
import com.lprevidente.orgcraft.office.application.handler.CreateOfficeHandler;
import com.lprevidente.orgcraft.office.application.handler.DeleteOfficeHandler;
import com.lprevidente.orgcraft.office.application.projection.OfficeView;
import com.lprevidente.orgcraft.office.application.query.OfficeQueryService;
import com.lprevidente.orgcraft.office.api.OfficeId;
import com.lprevidente.orgcraft.user.api.UserId;
import jakarta.validation.Valid;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/offices")
@RequiredArgsConstructor
class OfficeController {
  private final OfficeQueryService officeQueryService;
  private final CreateOfficeHandler createOfficeHandler;
  private final DeleteOfficeHandler deleteOfficeHandler;

  @GetMapping
  Collection<OfficeView> getOffices() {
    return officeQueryService.findAll();
  }

  @GetMapping("{id}")
  OfficeView getOffice(@PathVariable OfficeId id) {
    return officeQueryService.getById(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  OfficeId createOffice(
      @RequestBody @Valid CreateOffice command,
      @AuthenticationPrincipal(expression = "id") UserId creator) {
    return createOfficeHandler.handle(command, creator);
  }

  @DeleteMapping("{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void deleteOffice(@PathVariable OfficeId id) {
    deleteOfficeHandler.handle(new DeleteOffice(id));
  }
}
