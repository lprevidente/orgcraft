package com.lprevidente.orgcraft.organization.application.handler;

import com.lprevidente.orgcraft.organization.application.command.DeleteOrganization;
import com.lprevidente.orgcraft.organization.domain.Organizations;
import com.lprevidente.orgcraft.organization.domain.exception.OrganizationNotFoundException;
import com.lprevidente.orgcraft.tenancy.api.TenantContext;
import com.lprevidente.orgcraft.user.api.UserApi;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.cqrs.CommandHandler;
import org.jmolecules.ddd.annotation.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Service
@RequiredArgsConstructor
public class DeleteOrganizationHandler {

  private final Organizations organizations;
  private final UserApi users;

  @CommandHandler
  @Transactional
  public void handle(DeleteOrganization command) {
    final var organization =
        organizations
            .findById(command.id())
            .orElseThrow(() -> new OrganizationNotFoundException(command.id()));

    // An organization can only be deleted from within its own tenant: the tenant-scoped cascade
    // (team/office/user rows) targets whatever tenant is active on this thread, so it must match.
    final var tenant = TenantContext.get();
    Assert.state(
        tenant != null && tenant.value().equals(organization.getId().id()),
        "An organization can only be deleted from within its own tenant");

    // Cascade the tenant's users (organization -> user.api is allowed; user -> organization would
    // cycle, so team/office instead react to OrganizationDeleted on their own).
    users.deleteAllInCurrentTenant();

    // Publishes OrganizationDeleted: team/office modules wipe their aggregates synchronously (same
    // tenant + transaction), and the authorization module wipes organization:{id} in SpiceDB.
    organization.delete();
    organizations.delete(organization);
  }
}
