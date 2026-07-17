package com.lprevidente.orgcraft.team.application.handler;

import com.lprevidente.orgcraft.team.application.command.PromoteTeamMemberToAdmin;
import com.lprevidente.orgcraft.team.domain.TeamMemberId;
import com.lprevidente.orgcraft.team.domain.TeamMembers;
import com.lprevidente.orgcraft.team.domain.exception.TeamMemberNotFoundException;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.cqrs.CommandHandler;
import org.jmolecules.ddd.annotation.Service;

@Service
@RequiredArgsConstructor
public class PromoteTeamMemberToAdminHandler {
  private final TeamMembers teamMembers;

  @CommandHandler
  public void handle(PromoteTeamMemberToAdmin command) {
    final var membershipId = new TeamMemberId(command.teamId(), command.userId());
    final var teamMember =
        teamMembers
            .findById(membershipId)
            .orElseThrow(() -> new TeamMemberNotFoundException(membershipId));
    teamMember.promoteToAdmin();
    teamMembers.save(teamMember);
  }
}
