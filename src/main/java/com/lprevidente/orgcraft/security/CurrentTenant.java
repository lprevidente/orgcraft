package com.lprevidente.orgcraft.security;

import com.lprevidente.orgcraft.tenancy.api.TenantContext;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * Exposes the current tenant to method-security SpEL, so tenant-scoped checks read naturally, e.g.
 * {@code @PreAuthorize("hasPermission(@tenant.organizationId(), 'organization', 'manage')")}.
 *
 * <p>The tenant id <em>is</em> the owning organization's id (see {@code CreateOfficeHandler}).
 */
@Component("tenant")
class CurrentTenant {

  public UUID organizationId() {
    final var tenant = TenantContext.get();
    Assert.state(tenant != null, "No tenant in context");
    return tenant.value();
  }
}
