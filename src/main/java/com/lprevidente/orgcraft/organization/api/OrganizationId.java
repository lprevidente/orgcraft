package com.lprevidente.orgcraft.organization.api;

import com.lprevidente.orgcraft.common.identifier.Identifier;
import java.io.Serializable;
import java.util.UUID;
import org.jmolecules.ddd.annotation.ValueObject;
import org.springframework.util.Assert;

@ValueObject
public record OrganizationId(UUID id) implements Identifier, Serializable {

  public OrganizationId {
    Assert.notNull(id, "id must not be null");
  }

  public OrganizationId() {
    this(UUID.randomUUID());
  }
}
