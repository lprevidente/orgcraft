package com.lprevidente.orgcraft.office.domain.event;

import com.lprevidente.orgcraft.user.api.UserId;
import java.util.UUID;
import org.jmolecules.event.annotation.DomainEvent;

@DomainEvent
public record UnassignedUserFromOffice(UUID officeId, UserId userId) {}
