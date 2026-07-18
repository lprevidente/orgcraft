package com.lprevidente.orgcraft.common.authorization;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Port for resource-level authorization backed by a ReBAC store (SpiceDB).
 *
 * <p>Answers "which resources of a given type does this subject hold a permission on?" — the {@code
 * LookupResources} query, used to filter list endpoints without checking each row individually.
 */
public interface ResourceAuthorization {

  /**
   * The ids of {@code resourceType} resources on which {@code subjectId} holds {@code permission}.
   *
   * @return the accessible ids, or {@link Optional#empty()} when authorization is disabled and no
   *     filtering should be applied (the caller returns everything).
   */
  Optional<Set<UUID>> accessibleResourceIds(String resourceType, String permission, UUID subjectId);

  /**
   * Whether {@code subjectId} holds {@code permission} on the single resource {@code
   * resourceType:resourceId}. Returns {@code true} when authorization is disabled (permit-all).
   */
  boolean hasPermission(String resourceType, String resourceId, String permission, UUID subjectId);
}
