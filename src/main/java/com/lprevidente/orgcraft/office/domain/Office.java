package com.lprevidente.orgcraft.office.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Table;
import java.util.Objects;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.TenantId;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;
import org.jspecify.annotations.Nullable;
import org.springframework.util.Assert;

@Getter
@AggregateRoot
@Table(name = "offices")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Office {

  @Identity private OfficeId id;

  private String name;

  private Address address;

  @TenantId
  @Column(name = "tenant_id", nullable = false, updatable = false)
  private @Nullable String tenantId;

  public Office(String name, Address address) {
    Assert.notNull(name, "name must not be null");
    Assert.notNull(address, "address must not be null");
    this.id = new OfficeId();
    this.name = name;
    this.address = address;
  }

  @Override
  public final boolean equals(Object o) {
    if (!(o instanceof Office office)) return false;
    return Objects.equals(id, office.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
