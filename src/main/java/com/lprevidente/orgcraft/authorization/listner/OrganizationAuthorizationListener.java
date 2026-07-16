package com.lprevidente.orgcraft.authorization.listner;

import com.authzed.api.v1.PermissionsServiceGrpc.PermissionsServiceBlockingStub;
import com.authzed.api.v1.WriteRelationshipsRequest;
import com.lprevidente.orgcraft.organization.domain.event.OrganizationCreated;
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

    permissionsService.writeRelationships(
        WriteRelationshipsRequest.newBuilder()
            .addUpdates(touch(ORGANIZATION_RESOURCE, orgId, ADMIN_RELATION, USER_SUBJECT, founderId))
            .build());

    log.info("Wrote SpiceDB tuple organization:{}#admin@user:{}", orgId, founderId);
  }
}
