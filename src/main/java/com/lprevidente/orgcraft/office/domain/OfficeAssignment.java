package com.lprevidente.orgcraft.office.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.TenantId;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.AbstractAggregateRoot;
import com.lprevidente.orgcraft.office.domain.event.AssignedUserToOffice;
import com.lprevidente.orgcraft.office.domain.event.UnassignedUserFromOffice;
import com.lprevidente.orgcraft.user.api.UserId;

@Getter
@AggregateRoot
@Table(name = "office_assignments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OfficeAssignment extends AbstractAggregateRoot<OfficeAssignment> {

  @Identity private OfficeAssignmentId id;

  @AttributeOverride(name = "id", column = @Column(name = "office_id"))
  private OfficeId officeId;

  @AttributeOverride(name = "id", column = @Column(name = "user_id"))
  private UserId userId;

  private Instant assignedAt;

  private Instant unassignedAt;

  @TenantId
  @Column(name = "tenant_id", nullable = false, updatable = false)
  private @Nullable String tenantId;

  public OfficeAssignment(OfficeId officeId, UserId userId) {
    this.id = new OfficeAssignmentId();
    this.officeId = officeId;
    this.userId = userId;
    this.assignedAt = Instant.now();

    registerEvent(new AssignedUserToOffice(officeId.id(), userId));
  }

  public void unassign() {
    this.unassignedAt = Instant.now();
    registerEvent(new UnassignedUserFromOffice(officeId.id(), userId));
  }

  @Override
  public final boolean equals(Object o) {
    if (!(o instanceof OfficeAssignment that)) return false;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
