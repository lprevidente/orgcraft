package com.lprevidente.orgcraft.team.infrastructure.rest;

import com.lprevidente.orgcraft.team.application.query.TeamMemberQueryService;
import com.lprevidente.orgcraft.team.application.command.AddUserToTeam;
import com.lprevidente.orgcraft.team.application.command.PromoteTeamMemberToAdmin;
import com.lprevidente.orgcraft.team.application.command.RemoveUserFromTeam;
import com.lprevidente.orgcraft.team.application.command.RevokeTeamMemberAdmin;
import com.lprevidente.orgcraft.team.application.projection.TeamMemberView;
import com.lprevidente.orgcraft.team.application.handler.AddUserToTeamHandler;
import com.lprevidente.orgcraft.team.application.handler.PromoteTeamMemberToAdminHandler;
import com.lprevidente.orgcraft.team.application.handler.RemoveUserFromTeamHandler;
import com.lprevidente.orgcraft.team.application.handler.RevokeTeamMemberAdminHandler;
import com.lprevidente.orgcraft.team.api.TeamId;
import com.lprevidente.orgcraft.user.api.UserId;
import jakarta.validation.Valid;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teams/{teamId}/members")
@RequiredArgsConstructor
class TeamMemberController {

  private final TeamMemberQueryService teamMemberQueryService;
  private final AddUserToTeamHandler addUserToTeamHandler;
  private final RemoveUserFromTeamHandler removeUserFromTeamHandler;
  private final PromoteTeamMemberToAdminHandler promoteTeamMemberToAdminHandler;
  private final RevokeTeamMemberAdminHandler revokeTeamMemberAdminHandler;

  @GetMapping
  @PreAuthorize("hasPermission(#teamId, 'team', 'manage')")
  Collection<TeamMemberView> getTeamMembers(@PathVariable TeamId teamId) {
    return teamMemberQueryService.getTeamMembers(teamId);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasPermission(#teamId, 'team', 'manage')")
  void addMember(@PathVariable TeamId teamId, @RequestBody @Valid AddUserToTeam command) {
    addUserToTeamHandler.handle(command);
  }

  @DeleteMapping("{userId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasPermission(#teamId, 'team', 'manage')")
  void removeMemberFromTeam(@PathVariable TeamId teamId, @PathVariable UserId userId) {
    removeUserFromTeamHandler.handle(new RemoveUserFromTeam(teamId, userId));
  }

  // Only an org admin may promote a member to team admin.
  @PutMapping("{userId}/admin")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasPermission(@tenant.organizationId(), 'organization', 'manage')")
  void promoteToAdmin(@PathVariable TeamId teamId, @PathVariable UserId userId) {
    promoteTeamMemberToAdminHandler.handle(new PromoteTeamMemberToAdmin(teamId, userId));
  }

  @DeleteMapping("{userId}/admin")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasPermission(@tenant.organizationId(), 'organization', 'manage')")
  void revokeAdmin(@PathVariable TeamId teamId, @PathVariable UserId userId) {
    revokeTeamMemberAdminHandler.handle(new RevokeTeamMemberAdmin(teamId, userId));
  }
}
