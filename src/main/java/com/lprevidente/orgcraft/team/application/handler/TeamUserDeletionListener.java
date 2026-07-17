package com.lprevidente.orgcraft.team.application.handler;

import com.lprevidente.orgcraft.team.domain.Team;
import com.lprevidente.orgcraft.team.domain.TeamMembers;
import com.lprevidente.orgcraft.team.domain.Teams;
import com.lprevidente.orgcraft.user.domain.event.UserDeleted;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Removes the user's team memberships and orphans the creator of any team they created.
 *
 * <p>Synchronous (plain {@link EventListener}) on purpose: it must run on the deletion thread to
 * inherit the request's {@code TenantContext} (a {@code ThreadLocal}) and transaction. An async
 * module listener would run on a thread with no tenant and silently match nothing.
 */
@Slf4j
@Component
@RequiredArgsConstructor
class TeamUserDeletionListener {

  private final TeamMembers teamMembers;
  private final Teams teams;

  @EventListener
  void on(UserDeleted event) {
    final var userId = event.userId();

    final var memberships = teamMembers.findByIdUserId(userId);
    teamMembers.deleteAll(memberships);

    final var createdTeams = teams.findByCreator(userId);
    createdTeams.forEach(Team::removeCreator);
    teams.saveAll(createdTeams);

    log.info(
        "User {} deleted: removed {} team membership(s), orphaned creator on {} team(s)",
        userId.id(),
        memberships.size(),
        createdTeams.size());
  }
}
