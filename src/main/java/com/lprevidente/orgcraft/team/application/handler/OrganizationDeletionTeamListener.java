package com.lprevidente.orgcraft.team.application.handler;

import com.lprevidente.orgcraft.organization.domain.event.OrganizationDeleted;
import com.lprevidente.orgcraft.team.domain.TeamMembers;
import com.lprevidente.orgcraft.team.domain.Teams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Cascade-deletes the tenant's teams and memberships when its organization is deleted. Each team is
 * removed via {@code Team.delete()} so its {@code TeamDeleted} event wipes the SpiceDB resource.
 *
 * <p>Synchronous (plain {@link EventListener}) so it runs on the deletion thread, inheriting the
 * {@code TenantContext} and transaction — {@code findAll()} is then correctly scoped to the tenant.
 */
@Slf4j
@Component
@RequiredArgsConstructor
class OrganizationDeletionTeamListener {

  private final TeamMembers teamMembers;
  private final Teams teams;

  @EventListener
  void on(OrganizationDeleted event) {
    teamMembers.deleteAll(teamMembers.findAll());

    final var allTeams = teams.findAll();
    allTeams.forEach(
        team -> {
          team.delete();
          teams.delete(team);
        });

    log.info("Organization {} deleted: removed {} team(s)", event.id().id(), allTeams.size());
  }
}
