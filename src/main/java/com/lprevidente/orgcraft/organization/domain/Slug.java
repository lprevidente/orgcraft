package com.lprevidente.orgcraft.organization.domain;

import org.jmolecules.ddd.annotation.ValueObject;
import org.springframework.util.Assert;

@ValueObject
public record Slug(String value) {

  private static final String PATTERN = "^[a-z0-9](?:[a-z0-9-]{0,48}[a-z0-9])?$";

  public Slug {
    Assert.notNull(value, "value must not be null");
    Assert.isTrue(
        value.matches(PATTERN),
        "slug must be 1-50 lowercase alphanumeric characters or dashes, not starting or ending with a dash");
  }

  @Override
  public String toString() {
    return value;
  }
}
