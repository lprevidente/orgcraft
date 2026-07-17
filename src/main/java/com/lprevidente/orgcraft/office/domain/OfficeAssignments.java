package com.lprevidente.orgcraft.office.domain;

import java.util.List;
import java.util.Optional;
import org.jmolecules.ddd.annotation.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.lprevidente.orgcraft.user.api.UserId;

@Repository
public interface OfficeAssignments extends JpaRepository<OfficeAssignment, OfficeAssignmentId> {

  List<OfficeAssignment> findByUserId(UserId userId);

  Optional<OfficeAssignment> findByUserIdAndUnassignedAtIsNull(UserId userId);

  Optional<OfficeAssignment> findByOfficeIdAndUserIdAndUnassignedAtIsNull(
      OfficeId officeId, UserId userId);
}
