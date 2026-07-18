package com.lprevidente.orgcraft.team.application.handler;

import com.lprevidente.orgcraft.team.application.command.RevokeTeamMemberAdmin;
import com.lprevidente.orgcraft.team.domain.TeamMemberId;
import com.lprevidente.orgcraft.team.domain.TeamMembers;
import com.lprevidente.orgcraft.team.domain.exception.TeamMemberNotFoundException;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.cqrs.CommandHandler;
import org.jmolecules.ddd.annotation.Service;

@Service
@RequiredArgsConstructor
public class RevokeTeamMemberAdminHandler {
  private final TeamMembers teamMembers;

  @CommandHandler
  public void handle(RevokeTeamMemberAdmin command) {
    final var membershipId = new TeamMemberId(command.teamId(), command.userId());
    final var teamMember =
        teamMembers
            .findById(membershipId)
            .orElseThrow(() -> new TeamMemberNotFoundException(membershipId));
    teamMember.revokeAdmin();
    teamMembers.save(teamMember);
  }
}
