package com.lprevidente.orgcraft.authorization.listner;

import com.authzed.api.v1.PermissionsServiceGrpc.PermissionsServiceBlockingStub;
import com.authzed.api.v1.WriteRelationshipsRequest;
import com.lprevidente.orgcraft.office.domain.event.AssignedUserToOffice;
import com.lprevidente.orgcraft.office.domain.event.PromotedOfficeOccupantToAdmin;
import com.lprevidente.orgcraft.office.domain.event.RevokedOfficeOccupantAdmin;
import com.lprevidente.orgcraft.office.domain.event.UnassignedUserFromOffice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "orgcraft.spicedb.enabled", havingValue = "true")
class OfficeAssignmentAuthorizationListener extends BaseListener {

  public OfficeAssignmentAuthorizationListener(PermissionsServiceBlockingStub permissionsService) {
    super(permissionsService);
  }

  @ApplicationModuleListener
  void on(AssignedUserToOffice event) {
    final var officeId = event.officeId().id().toString();
    final var userId = event.userId().id().toString();

    permissionsService.writeRelationships(
        WriteRelationshipsRequest.newBuilder()
            .addUpdates(touch(OFFICE_RESOURCE, officeId, OCCUPANT_RELATION, USER_SUBJECT, userId))
            .build());

    log.info("Wrote SpiceDB tuple office:{}#occupant@user:{}", officeId, userId);
  }

  @ApplicationModuleListener
  void on(UnassignedUserFromOffice event) {
    final var officeId = event.officeId().id().toString();
    final var userId = event.userId().id().toString();

    // An unassigned user is neither occupant nor admin; drop both (admin delete is a no-op if not admin).
    permissionsService.writeRelationships(
        WriteRelationshipsRequest.newBuilder()
            .addUpdates(delete(OFFICE_RESOURCE, officeId, OCCUPANT_RELATION, USER_SUBJECT, userId))
            .addUpdates(delete(OFFICE_RESOURCE, officeId, ADMIN_RELATION, USER_SUBJECT, userId))
            .build());

    log.info("Deleted SpiceDB tuples office:{}#occupant@user:{} and office:{}#admin@user:{}", officeId, userId, officeId, userId);
  }

  @ApplicationModuleListener
  void on(PromotedOfficeOccupantToAdmin event) {
    final var officeId = event.officeId().id().toString();
    final var userId = event.userId().id().toString();

    permissionsService.writeRelationships(
        WriteRelationshipsRequest.newBuilder()
            .addUpdates(touch(OFFICE_RESOURCE, officeId, ADMIN_RELATION, USER_SUBJECT, userId))
            .build());

    log.info("Wrote SpiceDB tuple office:{}#admin@user:{}", officeId, userId);
  }

  @ApplicationModuleListener
  void on(RevokedOfficeOccupantAdmin event) {
    final var officeId = event.officeId().id().toString();
    final var userId = event.userId().id().toString();

    permissionsService.writeRelationships(
        WriteRelationshipsRequest.newBuilder()
            .addUpdates(delete(OFFICE_RESOURCE, officeId, ADMIN_RELATION, USER_SUBJECT, userId))
            .build());

    log.info("Deleted SpiceDB tuple office:{}#admin@user:{}", officeId, userId);
  }
}
