package com.lprevidente.orgcraft.authorization.listner;

import com.authzed.api.v1.PermissionsServiceGrpc.PermissionsServiceBlockingStub;
import com.authzed.api.v1.WriteRelationshipsRequest;
import com.lprevidente.orgcraft.user.domain.event.UserDeleted;
import com.lprevidente.orgcraft.user.domain.event.UserRegistered;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "orgcraft.spicedb.enabled", havingValue = "true")
class UserAuthorizationListener extends BaseListener {

  public UserAuthorizationListener(PermissionsServiceBlockingStub permissionsService) {
    super(permissionsService);
  }

  @ApplicationModuleListener
  void on(UserRegistered event) {
    final var orgId = event.organizationId().toString();
    final var userId = event.userId().id().toString();

    permissionsService.writeRelationships(
        WriteRelationshipsRequest.newBuilder()
            .addUpdates(touch(ORGANIZATION_RESOURCE, orgId, MEMBER_RELATION, USER_SUBJECT, userId))
            .build());

    log.info("Wrote SpiceDB tuple organization:{}#member@user:{}", orgId, userId);
  }

  @ApplicationModuleListener
  void on(UserDeleted event) {
    final var userId = event.userId().id().toString();

    // Evict the user as a subject from every resource type (one resource-scoped call each).
    permissionsService.deleteRelationships(
        deleteBySubject(ORGANIZATION_RESOURCE, USER_SUBJECT, userId));
    permissionsService.deleteRelationships(deleteBySubject(TEAM_RESOURCE, USER_SUBJECT, userId));
    permissionsService.deleteRelationships(deleteBySubject(OFFICE_RESOURCE, USER_SUBJECT, userId));

    log.info("Deleted all SpiceDB relationships where user:{} is the subject", userId);
  }
}
