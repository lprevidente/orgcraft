package com.lprevidente.orgcraft.user.domain.event;

import com.lprevidente.orgcraft.user.api.UserId;
import org.jmolecules.event.annotation.DomainEvent;

@DomainEvent
public record UserDeleted(UserId userId) {}
