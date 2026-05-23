package com.lprevidente.orgcraft.team.domain.exception;

import com.lprevidente.orgcraft.common.exception.DomainException;
import com.lprevidente.orgcraft.team.domain.TeamMemberId;
import org.springframework.http.HttpStatus;

/** Exception thrown when a user cannot be found. */
public class TeamMemberNotFoundException extends DomainException {

  public TeamMemberNotFoundException(TeamMemberId id) {
    super(
        "Team member with TeamId %s and UserId %s not found"
            .formatted(id.getTeamId().id(), id.getUserId().id()),
        "TEAM_MEMBER_NOT_FOUND",
        HttpStatus.NOT_FOUND,
        "Team Member Not Found");
  }
}
