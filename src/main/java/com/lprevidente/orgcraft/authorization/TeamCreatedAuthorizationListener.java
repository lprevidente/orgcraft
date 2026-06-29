package com.lprevidente.orgcraft.authorization;

import com.authzed.api.v1.ObjectReference;
import com.authzed.api.v1.PermissionsServiceGrpc.PermissionsServiceBlockingStub;
import com.authzed.api.v1.Relationship;
import com.authzed.api.v1.RelationshipUpdate;
import com.authzed.api.v1.SubjectReference;
import com.authzed.api.v1.WriteRelationshipsRequest;
import com.lprevidente.orgcraft.team.domain.event.TeamCreated;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "SpiceDBProperties", havingValue = "true")
class TeamCreatedAuthorizationListener {

  private static final String TEAM_RESOURCE = "team";
  private static final String USER_SUBJECT = "user";
  private static final String ADMIN_RELATION = "admin";
  private static final String SYSTEM_PRINCIPAL = "system";

  private final PermissionsServiceBlockingStub permissionsService;

  @ApplicationModuleListener
  void on(TeamCreated event) {
    final var teamId = event.id().toString();
    final var update =
        RelationshipUpdate.newBuilder()
            .setOperation(RelationshipUpdate.Operation.OPERATION_TOUCH)
            .setRelationship(
                Relationship.newBuilder()
                    .setResource(
                        ObjectReference.newBuilder()
                            .setObjectType(TEAM_RESOURCE)
                            .setObjectId(teamId))
                    .setRelation(ADMIN_RELATION)
                    .setSubject(
                        SubjectReference.newBuilder()
                            .setObject(
                                ObjectReference.newBuilder()
                                    .setObjectType(USER_SUBJECT)
                                    .setObjectId(SYSTEM_PRINCIPAL))))
            .build();

    permissionsService.writeRelationships(
        WriteRelationshipsRequest.newBuilder().addUpdates(update).build());
    log.info("Wrote SpiceDB tuple team:{}#admin@user:{}", teamId, SYSTEM_PRINCIPAL);
  }
}
