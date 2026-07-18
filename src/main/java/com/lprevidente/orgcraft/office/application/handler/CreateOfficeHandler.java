package com.lprevidente.orgcraft.office.application.handler;

import com.lprevidente.orgcraft.office.application.command.CreateOffice;
import com.lprevidente.orgcraft.office.application.command.CreateOfficeRes;
import com.lprevidente.orgcraft.office.domain.Office;
import com.lprevidente.orgcraft.office.domain.Offices;
import com.lprevidente.orgcraft.organization.api.OrganizationId;
import com.lprevidente.orgcraft.tenancy.api.TenantContext;
import com.lprevidente.orgcraft.user.api.UserId;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.cqrs.CommandHandler;
import org.jmolecules.ddd.annotation.Service;
import org.springframework.util.Assert;

@Service
@RequiredArgsConstructor
public class CreateOfficeHandler {
  private final Offices offices;

  @CommandHandler
  public CreateOfficeRes handle(CreateOffice command, UserId creator) {
    final var office = new Office(command.name(), command.address(), creator, currentOrganization());
    offices.save(office);
    return new CreateOfficeRes(office.getId());
  }

  private OrganizationId currentOrganization() {
    final var tenantId = TenantContext.get();
    Assert.state(tenantId != null, "No tenant in context");
    return new OrganizationId(tenantId.value());
  }
}
