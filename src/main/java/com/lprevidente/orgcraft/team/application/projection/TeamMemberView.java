package com.lprevidente.orgcraft.team.application.projection;

import com.lprevidente.orgcraft.team.domain.TeamMemberId;
import org.jmolecules.architecture.cqrs.QueryModel;

@QueryModel
public interface TeamMemberView {
  TeamMemberId getId();

  boolean isAdmin();
}
