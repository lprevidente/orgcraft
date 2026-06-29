package com.lprevidente.orgcraft.team.application.handler;

import com.lprevidente.orgcraft.organization.api.OrganizationId;
import com.lprevidente.orgcraft.team.api.TeamId;
import com.lprevidente.orgcraft.team.application.command.CreateTeam;
import com.lprevidente.orgcraft.team.domain.Team;
import com.lprevidente.orgcraft.team.domain.Teams;
import com.lprevidente.orgcraft.tenancy.api.TenantContext;
import com.lprevidente.orgcraft.user.api.UserId;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.cqrs.CommandHandler;
import org.jmolecules.ddd.annotation.Service;
import org.springframework.util.Assert;

@Service
@RequiredArgsConstructor
public class CreateTeamHandler {
  private final Teams teams;

  @CommandHandler
  public TeamId handle(CreateTeam command, UserId creator) {
    final var team = new Team(command.name(), creator, currentOrganization());
    teams.save(team);
    return team.getId();
  }

  private OrganizationId currentOrganization() {
    final var tenantId = TenantContext.get();
    Assert.state(tenantId != null, "No tenant in context");
    return new OrganizationId(tenantId.value());
  }
}
