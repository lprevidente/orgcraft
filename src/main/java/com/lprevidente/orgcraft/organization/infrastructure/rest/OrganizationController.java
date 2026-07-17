package com.lprevidente.orgcraft.organization.infrastructure.rest;

import com.lprevidente.orgcraft.organization.api.OrganizationId;
import com.lprevidente.orgcraft.organization.application.command.DeleteOrganization;
import com.lprevidente.orgcraft.organization.application.command.RegisterOrganization;
import com.lprevidente.orgcraft.organization.application.command.RegisterOrganizationRes;
import com.lprevidente.orgcraft.organization.application.handler.DeleteOrganizationHandler;
import com.lprevidente.orgcraft.organization.application.handler.RegisterOrganizationHandler;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
class OrganizationController {

  private final RegisterOrganizationHandler registerOrganizationHandler;
  private final DeleteOrganizationHandler deleteOrganizationHandler;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  RegisterOrganizationRes registerOrganization(@RequestBody @Valid RegisterOrganization command) {
    return registerOrganizationHandler.handle(command);
  }

  @DeleteMapping("{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void deleteOrganization(@PathVariable OrganizationId id) {
    deleteOrganizationHandler.handle(new DeleteOrganization(id));
  }
}
