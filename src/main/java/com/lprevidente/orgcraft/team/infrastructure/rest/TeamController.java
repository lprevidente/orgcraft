package com.lprevidente.orgcraft.team.infrastructure.rest;

import com.lprevidente.orgcraft.common.authorization.ResourceAuthorization;
import com.lprevidente.orgcraft.common.authorization.SpiceDbSchema.Permission;
import com.lprevidente.orgcraft.common.authorization.SpiceDbSchema.Type;
import com.lprevidente.orgcraft.team.application.query.TeamQueryService;
import com.lprevidente.orgcraft.team.application.command.CreateTeam;
import com.lprevidente.orgcraft.team.application.command.CreateTeamRes;
import com.lprevidente.orgcraft.team.application.command.DeleteTeam;
import com.lprevidente.orgcraft.team.application.projection.TeamView;
import com.lprevidente.orgcraft.team.application.handler.CreateTeamHandler;
import com.lprevidente.orgcraft.team.application.handler.DeleteTeamHandler;
import com.lprevidente.orgcraft.team.api.TeamId;
import com.lprevidente.orgcraft.user.api.UserId;
import jakarta.validation.Valid;
import java.util.Collection;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
class TeamController {

  private final TeamQueryService teamQueryService;
  private final CreateTeamHandler createTeamHandler;
  private final DeleteTeamHandler deleteTeamHandler;
  private final ResourceAuthorization authorization;

  @GetMapping
  Collection<TeamView> getTeams(@AuthenticationPrincipal(expression = "id") UserId user) {
    return authorization.accessibleResourceIds(Type.TEAM, Permission.VIEW, user.id())
        .map(ids -> ids.stream().map(TeamId::new).collect(Collectors.toSet()))
        .map(teamQueryService::findAllByIds)
        .orElseGet(teamQueryService::getTeams);
  }

  @GetMapping("{id}")
  @PreAuthorize("hasPermission(#id, 'team', 'view')")
  TeamView getTeam(@PathVariable TeamId id) {
    return teamQueryService.getById(id);
  }

  // A team is created under the current tenant's organization, so only a manager (admin) of that
  // organization may create one.
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasPermission(@tenant.organizationId(), 'organization', 'manage')")
  CreateTeamRes createTeam(
      @RequestBody @Valid CreateTeam command,
      @AuthenticationPrincipal(expression = "id") UserId creator) {
    return createTeamHandler.handle(command, creator);
  }

  @DeleteMapping("{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasPermission(#id, 'team', 'manage')")
  void deleteTeam(@PathVariable @Valid TeamId id) {
    deleteTeamHandler.handle(new DeleteTeam(id));
  }
}
