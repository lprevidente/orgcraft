package com.lprevidente.orgcraft.authorization.listner;

import com.authzed.api.v1.DeleteRelationshipsRequest;
import com.authzed.api.v1.PermissionsServiceGrpc.PermissionsServiceBlockingStub;
import com.authzed.api.v1.RelationshipFilter;
import com.authzed.api.v1.WriteRelationshipsRequest;
import com.lprevidente.orgcraft.common.authorization.SpiceDbSchema.Relation;
import com.lprevidente.orgcraft.common.authorization.SpiceDbSchema.Type;
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
            .addUpdates(touch(Type.ORGANIZATION, orgId, Relation.MEMBER, Type.USER, userId))
            .addUpdates(touch(Type.USER, userId, Relation.ORGANIZATION, Type.ORGANIZATION, orgId))
            .build());

    log.info(
        "Wrote SpiceDB tuples organization:{}#member@user:{}, user:{}#organization@organization:{}",
        orgId, userId, userId, orgId);
  }

  @ApplicationModuleListener
  void on(UserDeleted event) {
    final var userId = event.userId().id().toString();

    // Evict the user as a subject from every resource type (one resource-scoped call each).
    permissionsService.deleteRelationships(deleteBySubject(Type.ORGANIZATION, Type.USER, userId));
    permissionsService.deleteRelationships(deleteBySubject(Type.TEAM, Type.USER, userId));
    permissionsService.deleteRelationships(deleteBySubject(Type.OFFICE, Type.USER, userId));

    // Also evict the user as a resource (its own organization/office edges).
    permissionsService.deleteRelationships(
        DeleteRelationshipsRequest.newBuilder()
            .setRelationshipFilter(
                RelationshipFilter.newBuilder()
                    .setResourceType(Type.USER)
                    .setOptionalResourceId(userId))
            .build());

    log.info("Deleted all SpiceDB relationships involving user:{}", userId);
  }
}
