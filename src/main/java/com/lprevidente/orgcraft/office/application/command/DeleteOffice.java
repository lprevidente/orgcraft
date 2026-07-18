package com.lprevidente.orgcraft.office.application.command;

import com.lprevidente.orgcraft.office.api.OfficeId;
import jakarta.validation.constraints.NotNull;
import org.jmolecules.architecture.cqrs.Command;

@Command
public record DeleteOffice(@NotNull OfficeId id) {}
