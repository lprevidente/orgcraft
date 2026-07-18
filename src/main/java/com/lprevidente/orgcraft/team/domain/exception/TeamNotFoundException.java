package com.lprevidente.orgcraft.team.domain.exception;

import com.lprevidente.orgcraft.common.exception.DomainException;
import com.lprevidente.orgcraft.team.api.TeamId;
import org.springframework.http.HttpStatus;

/** Exception thrown when a user cannot be found. */
public class TeamNotFoundException extends DomainException {

  public TeamNotFoundException(TeamId teamId) {
    super(
        "Team with ID %s not found".formatted(teamId.id()),
        "TEAM_NOT_FOUND",
        HttpStatus.NOT_FOUND,
        "Team Not Found");
  }
}
