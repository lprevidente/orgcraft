package com.lprevidente.orgcraft.authorization.listner;

import com.authzed.api.v1.DeleteRelationshipsRequest;
import com.authzed.api.v1.PermissionsServiceGrpc.PermissionsServiceBlockingStub;
import com.authzed.api.v1.RelationshipFilter;
import com.authzed.api.v1.WriteRelationshipsRequest;
import com.lprevidente.orgcraft.common.authorization.SpiceDbSchema.Relation;
import com.lprevidente.orgcraft.common.authorization.SpiceDbSchema.Type;
import com.lprevidente.orgcraft.office.domain.event.OfficeCreated;
import com.lprevidente.orgcraft.office.domain.event.OfficeDeleted;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "orgcraft.spicedb.enabled", havingValue = "true")
class OfficeAuthorizationListener extends BaseListener {

  public OfficeAuthorizationListener(PermissionsServiceBlockingStub permissionsService) {
    super(permissionsService);
  }

  @ApplicationModuleListener
  void on(OfficeCreated event) {
    final var officeId = event.officeId().id().toString();
    final var creatorId = event.creator().id().toString();
    final var orgId = event.organization().id().toString();

    permissionsService.writeRelationships(
        WriteRelationshipsRequest.newBuilder()
            .addUpdates(
                touch(Type.OFFICE, officeId, Relation.ORGANIZATION, Type.ORGANIZATION, orgId))
            .addUpdates(touch(Type.OFFICE, officeId, Relation.CREATOR, Type.USER, creatorId))
            .build());

    log.info(
        "Wrote SpiceDB tuples office:{}#organization@organization:{}, office:{}#creator@user:{}",
        officeId,
        orgId,
        officeId,
        creatorId);
  }

  @ApplicationModuleListener
  void on(OfficeDeleted event) {
    final var officeId = event.officeId().id().toString();

    permissionsService.deleteRelationships(
        DeleteRelationshipsRequest.newBuilder()
            .setRelationshipFilter(
                RelationshipFilter.newBuilder()
                    .setResourceType(Type.OFFICE)
                    .setOptionalResourceId(officeId))
            .build());

    log.info("Deleted all SpiceDB relationships for office:{}", officeId);
  }
}
