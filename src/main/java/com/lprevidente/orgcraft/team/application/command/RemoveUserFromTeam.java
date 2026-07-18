package com.lprevidente.orgcraft.team.application.command;

import com.lprevidente.orgcraft.team.api.TeamId;
import com.lprevidente.orgcraft.user.api.UserId;
import jakarta.validation.constraints.NotNull;
import org.jmolecules.architecture.cqrs.Command;

@Command
public record RemoveUserFromTeam(@NotNull TeamId teamId, @NotNull UserId userId) {}
