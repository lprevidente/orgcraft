package com.lprevidente.orgcraft.organization.application.handler;

import com.lprevidente.orgcraft.organization.application.command.RegisterOrganization;
import com.lprevidente.orgcraft.organization.application.command.RegisterOrganizationRes;
import com.lprevidente.orgcraft.organization.domain.Organization;
import com.lprevidente.orgcraft.organization.domain.Organizations;
import com.lprevidente.orgcraft.organization.domain.Slug;
import com.lprevidente.orgcraft.tenancy.api.TenantContext;
import com.lprevidente.orgcraft.tenancy.api.TenantId;
import com.lprevidente.orgcraft.user.api.UserApi;
import com.lprevidente.orgcraft.user.api.UserId;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.cqrs.CommandHandler;
import org.jmolecules.ddd.annotation.Service;

@Service
@RequiredArgsConstructor
public class RegisterOrganizationHandler {

  private final Organizations organizations;
  private final UserApi users;

  @CommandHandler
  public RegisterOrganizationRes handle(RegisterOrganization command) {
    // Pre-allocate the founder id so it can be an intrinsic part of the Organization aggregate;
    // the founder user is then created under the new org's tenant with that same id.
    final var founderId = new UserId();
    final var organization = new Organization(command.name(), new Slug(command.slug()), founderId, organizations);

    try {
      TenantContext.set(TenantId.of(organization.getId().id()));
      users.register(founderId,
          command.founderFirstName(),
          command.founderLastName(),
          command.founderEmail(),
          command.founderPassword());
      organizations.save(organization);
      return new RegisterOrganizationRes(organization.getId(), founderId);
    } finally {
      TenantContext.clear();
    }
  }
}
