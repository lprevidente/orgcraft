package com.lprevidente.orgcraft.team.domain;

import com.lprevidente.orgcraft.team.api.TeamId;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import java.io.Serializable;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jmolecules.ddd.annotation.ValueObject;
import com.lprevidente.orgcraft.user.api.UserId;

/** Composite ID for TeamMembership consisting of id and userId */
@Getter
@ValueObject
@NoArgsConstructor
public class TeamMemberId implements Serializable {

  @AttributeOverride(name = "id", column = @Column(name = "team_id"))
  private TeamId teamId;

  @AttributeOverride(name = "id", column = @Column(name = "user_id"))
  private UserId userId;

  public TeamMemberId(TeamId teamId, UserId userId) {
    this.teamId = teamId;
    this.userId = userId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TeamMemberId that = (TeamMemberId) o;
    return Objects.equals(teamId, that.teamId) && Objects.equals(userId, that.userId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(teamId, userId);
  }
}
