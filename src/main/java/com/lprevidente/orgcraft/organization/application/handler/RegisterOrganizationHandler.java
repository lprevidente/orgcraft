package com.lprevidente.orgcraft.organization.application.handler;

import com.lprevidente.orgcraft.organization.application.command.RegisterOrganization;
import com.lprevidente.orgcraft.organization.application.command.RegisterOrganizationRes;
import com.lprevidente.orgcraft.organization.domain.Organization;
import com.lprevidente.orgcraft.organization.domain.Organizations;
import com.lprevidente.orgcraft.organization.domain.Slug;
import com.lprevidente.orgcraft.tenancy.api.TenantContext;
import com.lprevidente.orgcraft.user.api.UserApi;
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
    final var organization =
        new Organization(command.name(), new Slug(command.slug()), organizations);
    organizations.save(organization);

    try {
      TenantContext.set(organization.getId().id().toString());
      final var founderId =
          users.register(
              command.founderFirstName(),
              command.founderLastName(),
              command.founderEmail(),
              command.founderPassword());
      return new RegisterOrganizationRes(organization.getId(), founderId);
    } catch (RuntimeException e) {
      organizations.deleteById(organization.getId());
      throw e;
    } finally {
      TenantContext.clear();
    }
  }
}
