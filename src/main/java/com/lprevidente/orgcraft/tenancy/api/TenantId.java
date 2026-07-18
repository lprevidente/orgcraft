package com.lprevidente.orgcraft.tenancy.api;

import java.io.Serializable;
import java.util.UUID;
import org.springframework.util.Assert;

/**
 * Typed identifier of the current tenant. Kept generic (no dependency on the organization module) so
 * that {@code tenancy} stays infrastructure: callers translate to/from their own ids, e.g. {@code
 * TenantId.of(organizationId.id())}.
 */
public record TenantId(UUID value) implements Serializable {

  public TenantId {
    Assert.notNull(value, "value must not be null");
  }

  public static TenantId of(UUID value) {
    return new TenantId(value);
  }
}
