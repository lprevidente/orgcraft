package com.lprevidente.orgcraft.authorization.listner;

import com.authzed.api.v1.DeleteRelationshipsRequest;
import com.authzed.api.v1.PermissionsServiceGrpc;
import com.authzed.api.v1.RelationshipFilter;
import com.authzed.api.v1.WriteRelationshipsRequest;
import com.lprevidente.orgcraft.common.authorization.SpiceDbSchema.Relation;
import com.lprevidente.orgcraft.common.authorization.SpiceDbSchema.Type;
import com.lprevidente.orgcraft.team.domain.event.TeamCreated;
import com.lprevidente.orgcraft.team.domain.event.TeamDeleted;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "orgcraft.spicedb.enabled", havingValue = "true")
class TeamAuthorizationListener extends BaseListener {

  public TeamAuthorizationListener(PermissionsServiceGrpc.PermissionsServiceBlockingStub permissionsService) {
    super(permissionsService);
  }

  @ApplicationModuleListener
  void on(TeamCreated event) {
    final var teamId = event.id().id().toString();
    final var creatorId = event.creator().id().toString();
    final var orgId = event.organization().id().toString();

    permissionsService.writeRelationships(
        WriteRelationshipsRequest.newBuilder()
            .addUpdates(touch(Type.TEAM, teamId, Relation.ORGANIZATION, Type.ORGANIZATION, orgId))
            .addUpdates(touch(Type.TEAM, teamId, Relation.CREATOR, Type.USER, creatorId))
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
                    .setResourceType(Type.TEAM)
                    .setOptionalResourceId(teamId))
            .build());

    log.info("Deleted all SpiceDB relationships for team:{}", teamId);
  }
}
