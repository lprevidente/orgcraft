package com.lprevidente.orgcraft.authorization.listner;

import com.authzed.api.v1.PermissionsServiceGrpc.PermissionsServiceBlockingStub;
import com.authzed.api.v1.WriteRelationshipsRequest;
import com.lprevidente.orgcraft.common.authorization.SpiceDbSchema.Relation;
import com.lprevidente.orgcraft.common.authorization.SpiceDbSchema.Type;
import com.lprevidente.orgcraft.team.domain.event.AddedUserToTeam;
import com.lprevidente.orgcraft.team.domain.event.PromotedTeamMemberToAdmin;
import com.lprevidente.orgcraft.team.domain.event.RemovedUserFromTeam;
import com.lprevidente.orgcraft.team.domain.event.RevokedTeamMemberAdmin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "orgcraft.spicedb.enabled", havingValue = "true")
class TeamMemberAuthorizationListener extends BaseListener {

  public TeamMemberAuthorizationListener(PermissionsServiceBlockingStub permissionsService) {
    super(permissionsService);
  }

  @ApplicationModuleListener
  void on(AddedUserToTeam event) {
    final var teamId = event.teamId().id().toString();
    final var userId = event.userId().id().toString();

    permissionsService.writeRelationships(
        WriteRelationshipsRequest.newBuilder()
            .addUpdates(touch(Type.TEAM, teamId, Relation.MEMBER, Type.USER, userId))
            .build());

    log.info("Wrote SpiceDB tuple team:{}#member@user:{}", teamId, userId);
  }

  @ApplicationModuleListener
  void on(RemovedUserFromTeam event) {
    final var teamId = event.teamId().id().toString();
    final var userId = event.userId().id().toString();

    // A removed member holds neither relation; drop both (admin delete is a no-op if not admin).
    permissionsService.writeRelationships(
        WriteRelationshipsRequest.newBuilder()
            .addUpdates(delete(Type.TEAM, teamId, Relation.MEMBER, Type.USER, userId))
            .addUpdates(delete(Type.TEAM, teamId, Relation.ADMIN, Type.USER, userId))
            .build());

    log.info("Deleted SpiceDB tuples team:{}#member@user:{} and team:{}#admin@user:{}", teamId, userId, teamId, userId);
  }

  @ApplicationModuleListener
  void on(PromotedTeamMemberToAdmin event) {
    final var teamId = event.teamId().id().toString();
    final var userId = event.userId().id().toString();

    permissionsService.writeRelationships(
        WriteRelationshipsRequest.newBuilder()
            .addUpdates(touch(Type.TEAM, teamId, Relation.ADMIN, Type.USER, userId))
            .build());

    log.info("Wrote SpiceDB tuple team:{}#admin@user:{}", teamId, userId);
  }

  @ApplicationModuleListener
  void on(RevokedTeamMemberAdmin event) {
    final var teamId = event.teamId().id().toString();
    final var userId = event.userId().id().toString();

    permissionsService.writeRelationships(
        WriteRelationshipsRequest.newBuilder()
            .addUpdates(delete(Type.TEAM, teamId, Relation.ADMIN, Type.USER, userId))
            .build());

    log.info("Deleted SpiceDB tuple team:{}#admin@user:{}", teamId, userId);
  }
}
