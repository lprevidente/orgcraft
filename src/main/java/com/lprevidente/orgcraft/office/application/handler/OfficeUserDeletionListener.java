package com.lprevidente.orgcraft.office.application.handler;

import com.lprevidente.orgcraft.office.domain.Office;
import com.lprevidente.orgcraft.office.domain.OfficeAssignments;
import com.lprevidente.orgcraft.office.domain.Offices;
import com.lprevidente.orgcraft.user.domain.event.UserDeleted;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Removes the user's office assignments and orphans the creator of any office they created.
 *
 * <p>Synchronous (plain {@link EventListener}) on purpose: it must run on the deletion thread to
 * inherit the request's {@code TenantContext} (a {@code ThreadLocal}) and transaction. An async
 * module listener would run on a thread with no tenant and silently match nothing.
 */
@Slf4j
@Component
@RequiredArgsConstructor
class OfficeUserDeletionListener {

  private final OfficeAssignments officeAssignments;
  private final Offices offices;

  @EventListener
  void on(UserDeleted event) {
    final var userId = event.userId();

    final var assignments = officeAssignments.findByUserId(userId);
    officeAssignments.deleteAll(assignments);

    final var createdOffices = offices.findByCreator(userId);
    createdOffices.forEach(Office::removeCreator);
    offices.saveAll(createdOffices);

    log.info(
        "User {} deleted: removed {} office assignment(s), orphaned creator on {} office(s)",
        userId.id(),
        assignments.size(),
        createdOffices.size());
  }
}
