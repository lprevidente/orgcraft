package com.lprevidente.orgcraft.team.domain.event;

import com.lprevidente.orgcraft.team.api.TeamId;
import org.jmolecules.event.annotation.DomainEvent;

@DomainEvent
public record TeamCreated(TeamId id) {}
