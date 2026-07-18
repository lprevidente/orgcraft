package com.lprevidente.orgcraft.team.application.command;

import com.lprevidente.orgcraft.team.api.TeamId;
import jakarta.validation.constraints.NotNull;
import org.jmolecules.architecture.cqrs.Command;

@Command
public record DeleteTeam(@NotNull TeamId id) {}
