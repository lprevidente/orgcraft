package com.lprevidente.orgcraft.authorization.listner;

import com.authzed.api.v1.PermissionsServiceGrpc.PermissionsServiceBlockingStub;
import com.authzed.api.v1.WriteRelationshipsRequest;
import com.lprevidente.orgcraft.team.domain.event.AddedUserToTeam;
import com.lprevidente.orgcraft.team.domain.event.RemovedUserFromTeam;
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
            .addUpdates(touch(TEAM_RESOURCE, teamId, MEMBER_RELATION, USER_SUBJECT, userId))
            .build());

    log.info("Wrote SpiceDB tuple team:{}#member@user:{}", teamId, userId);
  }

  @ApplicationModuleListener
  void on(RemovedUserFromTeam event) {
    final var teamId = event.teamId().id().toString();
    final var userId = event.userId().id().toString();

    permissionsService.writeRelationships(
        WriteRelationshipsRequest.newBuilder()
            .addUpdates(delete(TEAM_RESOURCE, teamId, MEMBER_RELATION, USER_SUBJECT, userId))
            .build());

    log.info("Deleted SpiceDB tuple team:{}#member@user:{}", teamId, userId);
  }
}
