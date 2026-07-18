package com.lprevidente.orgcraft.team.application.projection;

import com.lprevidente.orgcraft.team.api.TeamId;
import org.jmolecules.architecture.cqrs.QueryModel;

@QueryModel
public interface TeamView {
  TeamId getId();

  String getName();
}
