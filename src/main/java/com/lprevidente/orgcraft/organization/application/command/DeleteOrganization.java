package com.lprevidente.orgcraft.organization.application.command;

import com.lprevidente.orgcraft.organization.api.OrganizationId;
import jakarta.validation.constraints.NotNull;
import org.jmolecules.architecture.cqrs.Command;

@Command
public record DeleteOrganization(@NotNull OrganizationId id) {}
