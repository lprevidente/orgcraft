package com.lprevidente.orgcraft.user.application.handler;

import com.lprevidente.orgcraft.tenancy.api.TenantContext;
import com.lprevidente.orgcraft.user.application.command.CreateUserReq;
import com.lprevidente.orgcraft.user.application.command.CreateUserRes;
import com.lprevidente.orgcraft.user.domain.*;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.cqrs.CommandHandler;
import org.jmolecules.ddd.annotation.Service;
import org.springframework.util.Assert;

@Service
@RequiredArgsConstructor
public class AddUserHandler {

  private final Users users;

  @CommandHandler
  public CreateUserRes handle(CreateUserReq command) {
    final var user =
        new User(
            command.firstName(),
            command.lastName(),
            Password.create(command.password()),
            new Email(command.email()),
            users);
    user.assignToOrganization(currentOrganizationId());
    users.save(user);
    return new CreateUserRes(user.getId());
  }

  private java.util.UUID currentOrganizationId() {
    final var tenantId = TenantContext.get();
    Assert.state(tenantId != null, "No tenant in context");
    return tenantId.value();
  }
}
