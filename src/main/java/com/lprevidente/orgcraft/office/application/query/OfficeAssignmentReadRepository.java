package com.lprevidente.orgcraft.office.application.query;

import com.lprevidente.orgcraft.office.domain.OfficeAssignment;
import com.lprevidente.orgcraft.office.domain.OfficeAssignmentId;
import com.lprevidente.orgcraft.office.api.OfficeId;
import com.lprevidente.orgcraft.user.api.UserId;
import java.util.List;
import org.springframework.data.repository.Repository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface OfficeAssignmentReadRepository
    extends Repository<OfficeAssignment, OfficeAssignmentId> {

  <T> List<T> findAllByOfficeIdAndUnassignedAtIsNull(OfficeId officeId, Class<T> projection);

  <T> List<T> findAllByUserId(UserId userId, Class<T> projection);
}
