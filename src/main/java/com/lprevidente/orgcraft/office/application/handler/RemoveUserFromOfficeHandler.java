package com.lprevidente.orgcraft.office.application.handler;

import com.lprevidente.orgcraft.office.application.command.RemoveUserFromOffice;
import com.lprevidente.orgcraft.office.domain.OfficeAssignments;
import com.lprevidente.orgcraft.office.api.OfficeId;
import com.lprevidente.orgcraft.user.api.UserId;
import com.lprevidente.orgcraft.office.domain.exception.OfficeAssignmentNotFoundException;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.cqrs.CommandHandler;
import org.jmolecules.ddd.annotation.Service;

@Service
@RequiredArgsConstructor
public class RemoveUserFromOfficeHandler {

  private final OfficeAssignments officeAssignments;

  @CommandHandler
  public void handle(RemoveUserFromOffice command) {
    final var officeId = new OfficeId(command.officeId());
    final var userId = new UserId(command.userId());
    final var assignment =
        officeAssignments
            .findByOfficeIdAndUserIdAndUnassignedAtIsNull(officeId, userId)
            .orElseThrow(() -> new OfficeAssignmentNotFoundException(officeId, userId));
    assignment.unassign();
    officeAssignments.save(assignment);
  }
}
