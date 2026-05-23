package com.lprevidente.orgcraft.organization.domain;

import com.lprevidente.orgcraft.organization.api.OrganizationId;
import com.lprevidente.orgcraft.organization.domain.event.OrganizationCreated;
import com.lprevidente.orgcraft.organization.domain.exception.SlugAlreadyInUseException;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.util.Assert;

@Getter
@AggregateRoot
@Table(name = "organizations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Organization extends AbstractAggregateRoot<Organization> {

  @Identity private OrganizationId id;

  private String name;

  @AttributeOverride(name = "value", column = @Column(name = "slug", unique = true))
  private Slug slug;

  private LocalDateTime createdAt;

  public Organization(String name, Slug slug, Organizations organizations) {
    Assert.hasText(name, "name must not be null or empty");
    Assert.notNull(slug, "slug must not be null");
    if (organizations.existsBySlug(slug)) throw new SlugAlreadyInUseException(slug);

    this.id = new OrganizationId();
    this.name = name;
    this.slug = slug;
    this.createdAt = LocalDateTime.now();

    registerEvent(new OrganizationCreated(this.id));
  }

  @Override
  public final boolean equals(Object o) {
    if (!(o instanceof Organization that)) return false;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
