package com.lprevidente.orgcraft.tenancy.api;

import org.jspecify.annotations.Nullable;

public final class TenantContext {

  private static final ThreadLocal<TenantId> CURRENT = new ThreadLocal<>();

  private TenantContext() {}

  public static void set(TenantId tenantId) {
    CURRENT.set(tenantId);
  }

  @Nullable
  public static TenantId get() {
    return CURRENT.get();
  }

  public static void clear() {
    CURRENT.remove();
  }
}
