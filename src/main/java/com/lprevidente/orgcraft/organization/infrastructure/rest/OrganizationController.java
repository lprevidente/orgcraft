package com.lprevidente.orgcraft.organization.infrastructure.rest;

import com.lprevidente.orgcraft.organization.application.command.RegisterOrganization;
import com.lprevidente.orgcraft.organization.application.command.RegisterOrganizationRes;
import com.lprevidente.orgcraft.organization.application.handler.RegisterOrganizationHandler;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  RegisterOrganizationRes registerOrganization(@RequestBody @Valid RegisterOrganization command) {
    return registerOrganizationHandler.handle(command);
  }
}
