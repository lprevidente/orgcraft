package com.lprevidente.orgcraft.office.application.projection;

import com.lprevidente.orgcraft.user.api.UserId;
import java.time.Instant;
import org.jmolecules.architecture.cqrs.QueryModel;

@QueryModel
public interface OfficeMemberView {
  UserId getUserId();

  Instant getAssignedAt();

  boolean isAdmin();
}
