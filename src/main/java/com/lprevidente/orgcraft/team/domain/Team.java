package com.lprevidente.orgcraft.team.domain;

import com.lprevidente.orgcraft.team.domain.event.TeamCreated;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Getter;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.util.Assert;

@Getter
@AggregateRoot
@Table(name = "teams")
public class Team extends AbstractAggregateRoot<Team> {

  @Identity private TeamId id;

  private String name;
  private LocalDateTime createdAt;

  protected Team() {}

  public Team(String name) {
    Assert.hasText(name, "name must not be null or empty");

    this.id = new TeamId();
    this.name = name;
    this.createdAt = LocalDateTime.now();

    registerEvent(new TeamCreated(this.id.id()));
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
