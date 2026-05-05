package com.lprevidente.orgcraft.team.domain.event;

import java.util.UUID;
import org.jmolecules.event.annotation.DomainEvent;

@DomainEvent
public record TeamCreated(UUID teamId) {}
