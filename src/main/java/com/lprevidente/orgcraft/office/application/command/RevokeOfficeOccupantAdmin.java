package com.lprevidente.orgcraft.office.application.command;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import org.jmolecules.architecture.cqrs.Command;

@Command
public record RevokeOfficeOccupantAdmin(@NotNull UUID officeId, @NotNull UUID userId) {}
