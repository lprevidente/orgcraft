package com.lprevidente.orgcraft.office.infrastructure.rest;

import com.lprevidente.orgcraft.common.authorization.ResourceAuthorization;
import com.lprevidente.orgcraft.common.authorization.SpiceDbSchema.Permission;
import com.lprevidente.orgcraft.common.authorization.SpiceDbSchema.Type;
import com.lprevidente.orgcraft.office.api.OfficeId;
import com.lprevidente.orgcraft.office.application.command.CreateOffice;
import com.lprevidente.orgcraft.office.application.command.DeleteOffice;
import com.lprevidente.orgcraft.office.application.handler.CreateOfficeHandler;
import com.lprevidente.orgcraft.office.application.handler.DeleteOfficeHandler;
import com.lprevidente.orgcraft.office.application.projection.OfficeView;
import com.lprevidente.orgcraft.office.application.query.OfficeQueryService;
import com.lprevidente.orgcraft.user.api.UserId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/offices")
@RequiredArgsConstructor
class OfficeController {
  private final OfficeQueryService officeQueryService;
  private final CreateOfficeHandler createOfficeHandler;
  private final DeleteOfficeHandler deleteOfficeHandler;
  private final ResourceAuthorization authorization;

  @GetMapping
  Collection<OfficeView> getOffices(@AuthenticationPrincipal(expression = "id") UserId user) {
    return authorization.accessibleResourceIds(Type.OFFICE, Permission.VIEW, user.id())
        .map(ids -> ids.stream().map(OfficeId::new).collect(Collectors.toSet()))
        .map(officeQueryService::findAllByIds)
        .orElseGet(officeQueryService::findAll);
  }

  @GetMapping("{id}")
  @PreAuthorize("hasPermission(#id, 'office', 'view')")
  OfficeView getOffice(@PathVariable OfficeId id) {
    return officeQueryService.getById(id);
  }

  // An office is created under the current tenant's organization, so only a manager (admin) of that
  // organization may create one.
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasPermission(@tenant.organizationId(), 'organization', 'manage')")
  OfficeId createOffice(
      @RequestBody @Valid CreateOffice command,
      @AuthenticationPrincipal(expression = "id") UserId creator) {
    return createOfficeHandler.handle(command, creator);
  }

  @DeleteMapping("{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasPermission(#id, 'office', 'manage')")
  void deleteOffice(@PathVariable OfficeId id) {
    deleteOfficeHandler.handle(new DeleteOffice(id));
  }
}
