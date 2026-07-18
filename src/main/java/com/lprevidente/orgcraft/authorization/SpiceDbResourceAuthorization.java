package com.lprevidente.orgcraft.authorization;

import com.authzed.api.v1.*;
import com.authzed.api.v1.CheckPermissionResponse.Permissionship;
import com.authzed.api.v1.PermissionsServiceGrpc.PermissionsServiceBlockingStub;
import com.lprevidente.orgcraft.common.authorization.ResourceAuthorization;
import com.lprevidente.orgcraft.common.authorization.SpiceDbSchema.Type;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * SpiceDB-backed {@link ResourceAuthorization}. Reads use {@code LookupResources} (which resources
 * can this subject reach?) and {@code CheckPermission} (does the subject reach this one resource?).
 * Both run fully consistent so a check reflects tuples written by a just-processed domain event.
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "orgcraft.spicedb.enabled", havingValue = "true")
class SpiceDbResourceAuthorization implements ResourceAuthorization {

  private final PermissionsServiceBlockingStub permissionsService;

  private static Consistency fullyConsistent() {
    return Consistency.newBuilder().setFullyConsistent(true).build();
  }

  private static SubjectReference userSubject(UUID subjectId) {
    return SubjectReference.newBuilder()
        .setObject(ObjectReference.newBuilder().setObjectType(Type.USER).setObjectId(subjectId.toString()))
        .build();
  }

  @Override
  public Optional<Set<UUID>> accessibleResourceIds(String resourceType, String permission, UUID subjectId) {
    final var request = LookupResourcesRequest.newBuilder()
        // Read-your-writes: a just-assigned occupant must show up in the list immediately.
        .setConsistency(fullyConsistent())
        .setResourceObjectType(resourceType)
        .setPermission(permission)
        .setSubject(userSubject(subjectId))
        .build();

    final var ids = new HashSet<UUID>();
    permissionsService.lookupResources(request)
        .forEachRemaining(response -> ids.add(UUID.fromString(response.getResourceObjectId())));
    return Optional.of(ids);
  }

  @Override
  public boolean hasPermission(String resourceType, String resourceId, String permission, UUID subjectId) {
    final var request = CheckPermissionRequest.newBuilder()
        .setConsistency(fullyConsistent())
        .setResource(ObjectReference.newBuilder().setObjectType(resourceType).setObjectId(resourceId))
        .setPermission(permission)
        .setSubject(userSubject(subjectId))
        .build();

    return permissionsService.checkPermission(request)
        .getPermissionship() == Permissionship.PERMISSIONSHIP_HAS_PERMISSION;
  }
}
