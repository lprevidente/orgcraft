package com.lprevidente.orgcraft.office.application.query;

import com.lprevidente.orgcraft.office.application.projection.OfficeAssignmentView;
import com.lprevidente.orgcraft.office.application.projection.OfficeMemberView;
import com.lprevidente.orgcraft.office.api.OfficeId;
import com.lprevidente.orgcraft.user.api.UserId;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.jmolecules.ddd.annotation.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OfficeAssignmentQueryService {

  private final OfficeAssignmentReadRepository officeAssignments;

  @Transactional(readOnly = true)
  public Collection<OfficeMemberView> getCurrentMembers(OfficeId officeId) {
    return officeAssignments.findAllByOfficeIdAndUnassignedAtIsNull(officeId, OfficeMemberView.class);
  }

  @Transactional(readOnly = true)
  public Collection<OfficeAssignmentView> getAssignmentHistory(UserId userId) {
    return officeAssignments.findAllByUserId(userId, OfficeAssignmentView.class);
  }
}
