package com.lprevidente.orgcraft.team.api;

import com.lprevidente.orgcraft.common.identifier.Identifier;
import java.io.Serializable;
import java.util.UUID;
import org.jmolecules.ddd.annotation.ValueObject;
import org.springframework.util.Assert;

@ValueObject
public record TeamId(UUID id) implements Serializable, Identifier {

  public TeamId {
    Assert.notNull(id, "id must not be null");
  }

  public TeamId() {
    this(UUID.randomUUID());
  }
}
