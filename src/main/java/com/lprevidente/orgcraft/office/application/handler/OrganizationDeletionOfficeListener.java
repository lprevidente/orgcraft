package com.lprevidente.orgcraft.office.application.handler;

import com.lprevidente.orgcraft.office.domain.OfficeAssignments;
import com.lprevidente.orgcraft.office.domain.Offices;
import com.lprevidente.orgcraft.organization.domain.event.OrganizationDeleted;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Cascade-deletes the tenant's offices and assignments when its organization is deleted. Each office
 * is removed via {@code Office.delete()} so its {@code OfficeDeleted} event wipes the SpiceDB
 * resource.
 *
 * <p>Synchronous (plain {@link EventListener}) so it runs on the deletion thread, inheriting the
 * {@code TenantContext} and transaction — {@code findAll()} is then correctly scoped to the tenant.
 */
@Slf4j
@Component
@RequiredArgsConstructor
class OrganizationDeletionOfficeListener {

  private final OfficeAssignments officeAssignments;
  private final Offices offices;

  @EventListener
  void on(OrganizationDeleted event) {
    officeAssignments.deleteAll(officeAssignments.findAll());

    final var allOffices = offices.findAll();
    allOffices.forEach(
        office -> {
          office.delete();
          offices.delete(office);
        });

    log.info("Organization {} deleted: removed {} office(s)", event.id().id(), allOffices.size());
  }
}
