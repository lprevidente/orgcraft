package com.lprevidente.orgcraft.tenancy.api;

import org.jspecify.annotations.Nullable;

public final class TenantContext {

  private static final ThreadLocal<String> CURRENT = new ThreadLocal<>();

  private TenantContext() {}

  public static void set(String tenantId) {
    CURRENT.set(tenantId);
  }

  @Nullable
  public static String get() {
    return CURRENT.get();
  }

  public static void clear() {
    CURRENT.remove();
  }
}
