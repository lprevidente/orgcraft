package com.lprevidente.orgcraft.team.application.query;

import com.lprevidente.orgcraft.team.api.TeamId;
import com.lprevidente.orgcraft.team.application.projection.TeamView;
import com.lprevidente.orgcraft.team.domain.exception.TeamNotFoundException;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jmolecules.ddd.annotation.Service;

@Service
@RequiredArgsConstructor
public class TeamQueryService {
  private final TeamReadRepository teams;

  public Collection<TeamView> getTeams() {
    return teams.findAllBy(TeamView.class);
  }

  public Collection<TeamView> findAllByIds(Collection<TeamId> ids) {
    if (ids.isEmpty()) {
      return List.of();
    }
    return teams.findByIdIn(ids, TeamView.class);
  }

  public TeamView getById(TeamId id) {
    return teams.findById(id, TeamView.class).orElseThrow(() -> new TeamNotFoundException(id));
  }
}
