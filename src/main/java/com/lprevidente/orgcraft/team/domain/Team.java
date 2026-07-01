package com.lprevidente.orgcraft.team.domain;

import com.lprevidente.orgcraft.organization.api.OrganizationId;
import com.lprevidente.orgcraft.team.api.TeamId;
import com.lprevidente.orgcraft.team.domain.event.TeamCreated;
import com.lprevidente.orgcraft.team.domain.event.TeamDeleted;
import com.lprevidente.orgcraft.user.api.UserId;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.TenantId;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@AggregateRoot
@Table(name = "teams")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Team extends AbstractAggregateRoot<Team> {

  @Identity private TeamId id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false, columnDefinition = "timestamp")
  private LocalDateTime createdAt;

  @AttributeOverride(name = "id", column = @Column(name = "creator_id", nullable = false, updatable = false))
  private UserId creator;

  @TenantId
  @Column(name = "tenant_id", nullable = false, updatable = false)
  private String tenantId;

  public Team(String name, UserId creator, OrganizationId organization) {
    Assert.hasText(name, "name must not be null or empty");
    Assert.notNull(creator, "creator must not be null");
    Assert.notNull(organization, "organization must not be null");

    this.id = new TeamId();
    this.name = name;
    this.createdAt = LocalDateTime.now();
    this.creator = creator;

    // organization == tenant; passed explicitly because @TenantId is only set on persist,
    // and SpiceDB needs it now to wire team -> organization in the published event.
    registerEvent(new TeamCreated(this.id, creator, organization));
  }

  /**
   * Registers the {@link TeamDeleted} event; call before removing the aggregate via its repository.
   */
  public void delete() {
    registerEvent(new TeamDeleted(this.id));
  }

  @Override
  public final boolean equals(Object o) {
    if (!(o instanceof Team that)) return false;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
