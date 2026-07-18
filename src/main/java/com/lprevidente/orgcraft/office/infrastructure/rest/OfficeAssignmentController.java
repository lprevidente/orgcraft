package com.lprevidente.orgcraft.office.infrastructure.rest;

import com.lprevidente.orgcraft.office.api.OfficeId;
import com.lprevidente.orgcraft.office.application.command.AssignUserToOffice;
import com.lprevidente.orgcraft.office.application.command.PromoteOfficeOccupantToAdmin;
import com.lprevidente.orgcraft.office.application.command.RemoveUserFromOffice;
import com.lprevidente.orgcraft.office.application.command.RevokeOfficeOccupantAdmin;
import com.lprevidente.orgcraft.office.application.handler.AssignUserToOfficeHandler;
import com.lprevidente.orgcraft.office.application.handler.PromoteOfficeOccupantToAdminHandler;
import com.lprevidente.orgcraft.office.application.handler.RemoveUserFromOfficeHandler;
import com.lprevidente.orgcraft.office.application.handler.RevokeOfficeOccupantAdminHandler;
import com.lprevidente.orgcraft.office.application.projection.OfficeAssignmentView;
import com.lprevidente.orgcraft.office.application.projection.OfficeMemberView;
import com.lprevidente.orgcraft.office.application.query.OfficeAssignmentQueryService;
import com.lprevidente.orgcraft.office.domain.OfficeAssignmentId;
import com.lprevidente.orgcraft.user.api.UserId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
class OfficeAssignmentController {

  private final OfficeAssignmentQueryService queryService;
  private final AssignUserToOfficeHandler assignHandler;
  private final RemoveUserFromOfficeHandler removeHandler;
  private final PromoteOfficeOccupantToAdminHandler promoteHandler;
  private final RevokeOfficeOccupantAdminHandler revokeHandler;

  @GetMapping("/api/v1/offices/{officeId}/members")
  @PreAuthorize("hasPermission(#officeId, 'office', 'manage')")
  Collection<OfficeMemberView> getCurrentMembers(@PathVariable OfficeId officeId) {
    return queryService.getCurrentMembers(officeId);
  }

  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/api/v1/offices/{officeId}/members")
  @PreAuthorize("hasPermission(#officeId, 'office', 'manage')")
  OfficeAssignmentId assignUser(@PathVariable UUID officeId, @RequestBody @Valid AssignUserToOffice command) {
    return assignHandler.handle(command);
  }

  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/api/v1/offices/{officeId}/members/{userId}")
  @PreAuthorize("hasPermission(#officeId, 'office', 'manage')")
  void removeUser(@PathVariable UUID officeId, @PathVariable UserId userId) {
    removeHandler.handle(new RemoveUserFromOffice(officeId, userId.id()));
  }

  @GetMapping("/api/v1/users/{userId}/office-history")
  Collection<OfficeAssignmentView> getAssignmentHistory(@PathVariable UserId userId) {
    return queryService.getAssignmentHistory(userId);
  }

  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PutMapping("/api/v1/offices/{officeId}/members/{userId}/admin")
  @PreAuthorize("hasPermission(@tenant.organizationId(), 'organization', 'manage')")
  void promoteToAdmin(@PathVariable UUID officeId, @PathVariable UserId userId) {
    promoteHandler.handle(new PromoteOfficeOccupantToAdmin(officeId, userId.id()));
  }

  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/api/v1/offices/{officeId}/members/{userId}/admin")
  @PreAuthorize("hasPermission(@tenant.organizationId(), 'organization', 'manage')")
  void revokeAdmin(@PathVariable UUID officeId, @PathVariable UserId userId) {
    revokeHandler.handle(new RevokeOfficeOccupantAdmin(officeId, userId.id()));
  }
}
