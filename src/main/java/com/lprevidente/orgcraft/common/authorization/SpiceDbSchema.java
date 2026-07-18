package com.lprevidente.orgcraft.common.authorization;

/**
 * Java mirror of {@code resources/spicedb/schema.zed} — the single source of truth for SpiceDB
 * object types, relations and permissions.
 *
 * <p>Shared by the two sides that speak to SpiceDB: relationship <em>writers</em> (the event
 * listeners in the {@code authorization} module) and permission <em>readers</em> (controllers via
 * {@link ResourceAuthorization}). Naming a resource type or permission goes through here, so a
 * rename in the {@code .zed} schema is a single-place change on the Java side and a typo cannot
 * silently target a non-existent object.
 *
 * <p><strong>Keep in lockstep with {@code schema.zed}.</strong>
 */
public final class SpiceDbSchema {

  private SpiceDbSchema() {}

  /** Object types — used both as resource types and as subject types. */
  public static final class Type {
    public static final String USER = "user";
    public static final String ORGANIZATION = "organization";
    public static final String TEAM = "team";
    public static final String OFFICE = "office";

    private Type() {}
  }

  /** Relation names — stored edges, written as relationships. */
  public static final class Relation {
    public static final String ORGANIZATION = "organization";
    public static final String OFFICE = "office";
    public static final String CREATOR = "creator";
    public static final String ADMIN = "admin";
    public static final String MEMBER = "member";
    public static final String OCCUPANT = "occupant";

    private Relation() {}
  }

  /** Permission names — computed, evaluated via CheckPermission / LookupResources. */
  public static final class Permission {
    public static final String VIEW = "view";
    public static final String MANAGE = "manage";

    private Permission() {}
  }
}
