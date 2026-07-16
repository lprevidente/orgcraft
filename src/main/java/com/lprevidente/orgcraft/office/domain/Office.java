package com.lprevidente.orgcraft.office.domain;

import com.lprevidente.orgcraft.office.domain.event.OfficeCreated;
import com.lprevidente.orgcraft.office.domain.event.OfficeDeleted;
import com.lprevidente.orgcraft.organization.api.OrganizationId;
import com.lprevidente.orgcraft.user.api.UserId;
import jakarta.persistence.AttributeOverride;
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
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.util.Assert;

@Getter
@AggregateRoot
@Table(name = "offices")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Office extends AbstractAggregateRoot<Office> {

  @Identity private OfficeId id;

  private String name;

  private Address address;

  @AttributeOverride(name = "id", column = @Column(name = "creator_id", nullable = false, updatable = false))
  private UserId creator;

  @TenantId
  @Column(name = "tenant_id", nullable = false, updatable = false)
  private @Nullable String tenantId;

  public Office(String name, Address address, UserId creator, OrganizationId organization) {
    Assert.notNull(name, "name must not be null");
    Assert.notNull(address, "address must not be null");
    Assert.notNull(creator, "creator must not be null");
    Assert.notNull(organization, "organization must not be null");
    this.id = new OfficeId();
    this.name = name;
    this.address = address;
    this.creator = creator;

    // organization == tenant; passed explicitly (like Team) so the event can wire office -> org.
    registerEvent(new OfficeCreated(this.id.id(), creator, organization));
  }

  /** Registers the {@link OfficeDeleted} event; call before removing the aggregate. */
  public void delete() {
    registerEvent(new OfficeDeleted(this.id.id()));
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
