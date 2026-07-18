package com.lprevidente.orgcraft.authorization.listner;

import com.authzed.api.v1.DeleteRelationshipsRequest;
import com.authzed.api.v1.PermissionsServiceGrpc.PermissionsServiceBlockingStub;
import com.authzed.api.v1.RelationshipFilter;
import com.authzed.api.v1.WriteRelationshipsRequest;
import com.lprevidente.orgcraft.common.authorization.SpiceDbSchema.Relation;
import com.lprevidente.orgcraft.common.authorization.SpiceDbSchema.Type;
import com.lprevidente.orgcraft.organization.domain.event.OrganizationCreated;
import com.lprevidente.orgcraft.organization.domain.event.OrganizationDeleted;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "orgcraft.spicedb.enabled", havingValue = "true")
class OrganizationAuthorizationListener extends BaseListener {

  public OrganizationAuthorizationListener(PermissionsServiceBlockingStub permissionsService) {
    super(permissionsService);
  }

  @ApplicationModuleListener
  void on(OrganizationCreated event) {
    final var orgId = event.id().id().toString();
    final var founderId = event.founder().id().toString();

    // Founder is both an admin and a member of the organization.
    permissionsService.writeRelationships(
        WriteRelationshipsRequest.newBuilder()
            .addUpdates(touch(Type.ORGANIZATION, orgId, Relation.ADMIN, Type.USER, founderId))
            .addUpdates(touch(Type.ORGANIZATION, orgId, Relation.MEMBER, Type.USER, founderId))
            .build());

    log.info(
        "Wrote SpiceDB tuples organization:{}#admin@user:{}, organization:{}#member@user:{}",
        orgId,
        founderId,
        orgId,
        founderId);
  }

  @ApplicationModuleListener
  void on(OrganizationDeleted event) {
    final var orgId = event.id().id().toString();

    // Removes the org's own tuples (admin/member). Teams and offices under it are wiped by their
    // own TeamDeleted/OfficeDeleted events raised during the cascade.
    permissionsService.deleteRelationships(
        DeleteRelationshipsRequest.newBuilder()
            .setRelationshipFilter(
                RelationshipFilter.newBuilder()
                    .setResourceType(Type.ORGANIZATION)
                    .setOptionalResourceId(orgId))
            .build());

    log.info("Deleted all SpiceDB relationships for organization:{}", orgId);
  }
}
