package com.lprevidente.orgcraft.security;

import com.lprevidente.orgcraft.common.authorization.ResourceAuthorization;
import com.lprevidente.orgcraft.common.identifier.Identifier;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * Bridges Spring Security's {@code hasPermission(id, targetType, permission)} SpEL to the ReBAC
 * store, so any endpoint can be guarded declaratively, e.g.
 * {@code @PreAuthorize("hasPermission(#id, 'office', 'view')")}.
 *
 * <p>{@code id} may be a domain {@link Identifier} (its UUID is used as the SpiceDB object id) or
 * any value whose {@code toString()} is the object id.
 */
@Component
@RequiredArgsConstructor
class ResourceAuthorizationPermissionEvaluator implements PermissionEvaluator {

  private final ResourceAuthorization authorization;

  @Override
  public boolean hasPermission(
      Authentication authentication,
      Serializable targetId,
      String targetType,
      Object permission) {
    if (!(authentication.getPrincipal() instanceof UserDetailsView principal)) {
      return false;
    }
    final var resourceId = targetId instanceof Identifier identifier ? identifier.id().toString() : targetId.toString();
    return authorization.hasPermission(targetType, resourceId, permission.toString(), principal.getId().id());
  }

  @Override
  public boolean hasPermission(Authentication authentication, @Nullable Object targetDomainObject, Object permission) {
    throw new UnsupportedOperationException(
        "Object-based checks are unsupported; use hasPermission(#id, targetType, permission)");
  }
}
