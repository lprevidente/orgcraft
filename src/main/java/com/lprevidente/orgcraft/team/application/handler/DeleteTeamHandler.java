package com.lprevidente.orgcraft.team.application.handler;

import com.lprevidente.orgcraft.team.application.command.DeleteTeam;
import com.lprevidente.orgcraft.team.domain.Teams;
import com.lprevidente.orgcraft.team.domain.exception.TeamNotFoundException;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.cqrs.CommandHandler;
import org.jmolecules.ddd.annotation.Service;

@Service
@RequiredArgsConstructor
public class DeleteTeamHandler {
  private final Teams teams;

  @CommandHandler
  public void handle(DeleteTeam command) {
    final var team =
        teams
            .findById(command.id()) //
            .orElseThrow(() -> new TeamNotFoundException(command.id()));
    team.delete();
    teams.delete(team);
  }
}
