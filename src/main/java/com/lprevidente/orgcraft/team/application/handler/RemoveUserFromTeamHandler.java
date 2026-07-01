package com.lprevidente.orgcraft.team.application.handler;

import com.lprevidente.orgcraft.team.application.command.RemoveUserFromTeam;
import com.lprevidente.orgcraft.team.domain.*;
import com.lprevidente.orgcraft.team.domain.exception.TeamMemberNotFoundException;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.cqrs.CommandHandler;
import org.jmolecules.ddd.annotation.Service;

@Service
@RequiredArgsConstructor
public class RemoveUserFromTeamHandler {
  private final TeamMembers teamMembers;

  @CommandHandler
  public void handle(RemoveUserFromTeam command) {
    final var membershipId = new TeamMemberId(command.teamId(), command.userId());
    final var teamMember =
        teamMembers
            .findById(membershipId)
            .orElseThrow(() -> new TeamMemberNotFoundException(membershipId));
    teamMember.remove();
    teamMembers.delete(teamMember);
  }
}
