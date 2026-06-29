package com.lprevidente.orgcraft.authorization.listner;

import com.authzed.api.v1.DeleteRelationshipsRequest;
import com.authzed.api.v1.PermissionsServiceGrpc.PermissionsServiceBlockingStub;
import com.authzed.api.v1.RelationshipFilter;
import com.authzed.api.v1.WriteRelationshipsRequest;
import com.lprevidente.orgcraft.team.domain.event.TeamCreated;
import com.lprevidente.orgcraft.team.domain.event.TeamDeleted;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "orgcraft.spicedb.enabled", havingValue = "true")
class TeamAuthorizationListener extends BaseListener {

  private static final String TEAM_RESOURCE = "team";
  private static final String USER_SUBJECT = "user";
  private static final String ORGANIZATION_SUBJECT = "organization";
  private static final String CREATOR_RELATION = "creator";
  private static final String ORGANIZATION_RELATION = "organization";

  private final PermissionsServiceBlockingStub permissionsService;

  @ApplicationModuleListener
  void on(TeamCreated event) {
    final var teamId = event.id().id().toString();
    final var creatorId = event.creator().id().toString();
    final var orgId = event.organization().id().toString();

    permissionsService.writeRelationships(
        WriteRelationshipsRequest.newBuilder()
            .addUpdates(touch(TEAM_RESOURCE, teamId, ORGANIZATION_RELATION, ORGANIZATION_SUBJECT, orgId))
            .addUpdates(touch(TEAM_RESOURCE, teamId, CREATOR_RELATION, USER_SUBJECT, creatorId))
            .build());

    log.info(
        "Wrote SpiceDB tuples team:{}#organization@organization:{}, team:{}#creator@user:{}",
        teamId,
        orgId,
        teamId,
        creatorId);
  }

  @ApplicationModuleListener
  void on(TeamDeleted event) {
    final var teamId = event.id().id().toString();

    // Delete-by-filter removes every tuple where the team is the resource
    // (organization, creator, admin, member, ...) in one idempotent call.
    permissionsService.deleteRelationships(
        DeleteRelationshipsRequest.newBuilder()
            .setRelationshipFilter(
                RelationshipFilter.newBuilder()
                    .setResourceType(TEAM_RESOURCE)
                    .setOptionalResourceId(teamId))
            .build());

    log.info("Deleted all SpiceDB relationships for team:{}", teamId);
  }
}
