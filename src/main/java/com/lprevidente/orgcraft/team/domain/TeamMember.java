package com.lprevidente.orgcraft.team.domain;

import com.lprevidente.orgcraft.team.api.TeamId;
import com.lprevidente.orgcraft.team.domain.event.AddedUserToTeam;
import com.lprevidente.orgcraft.team.domain.event.RemovedUserFromTeam;
import com.lprevidente.orgcraft.user.api.UserApi;
import com.lprevidente.orgcraft.user.api.UserId;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.TenantId;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@AggregateRoot
@Table(name = "team_members")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamMember extends AbstractAggregateRoot<TeamMember> {

  @Identity private TeamMemberId id;

  @Column(nullable = false, columnDefinition = "timestamp")
  private LocalDateTime joinedAt;

  @TenantId
  @Column(name = "tenant_id", nullable = false, updatable = false)
  private @Nullable String tenantId;

  public TeamMember(
      TeamId teamId, //
      UserId userId,
      Teams teams,
      UserApi users,
      TeamMembers teamMembers) {
    Assert.notNull(teamId, "teamId must not be null");
    Assert.notNull(userId, "userId must not be null");

    Assert.isTrue(teams.existsById(teamId), "team does not exist");
    Assert.isTrue(users.existsById(userId.id()), "user does not exist");

    this.id = new TeamMemberId(teamId, userId);

    Assert.isTrue(!teamMembers.existsById(id), "User is already a member of this team");

    this.joinedAt = LocalDateTime.now();

    registerEvent(new AddedUserToTeam(teamId, userId));
  }

  /** Registers the {@link RemovedUserFromTeam} event; call before removing the aggregate. */
  public void remove() {
    registerEvent(new RemovedUserFromTeam(id.getTeamId(), id.getUserId()));
  }

  public TeamId getTeamId() {
    return id.getTeamId();
  }

  public UserId getUserId() {
    return id.getUserId();
  }

  @Override
  public final boolean equals(Object o) {
    if (!(o instanceof TeamMember that)) return false;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
