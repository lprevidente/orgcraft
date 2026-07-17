package com.lprevidente.orgcraft.office.application.projection;

import com.lprevidente.orgcraft.office.domain.OfficeAssignmentId;
import com.lprevidente.orgcraft.office.api.OfficeId;
import com.lprevidente.orgcraft.user.api.UserId;
import java.time.Instant;
import org.jmolecules.architecture.cqrs.QueryModel;
import org.jspecify.annotations.Nullable;

@QueryModel
public interface OfficeAssignmentView {
  OfficeAssignmentId getId();

  OfficeId getOfficeId();

  UserId getUserId();

  Instant getAssignedAt();

  @Nullable Instant getUnassignedAt();

  boolean isAdmin();
}
