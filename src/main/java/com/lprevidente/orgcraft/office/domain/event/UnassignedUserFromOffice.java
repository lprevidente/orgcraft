package com.lprevidente.orgcraft.office.domain.event;

import com.lprevidente.orgcraft.office.api.OfficeId;
import com.lprevidente.orgcraft.user.api.UserId;
import org.jmolecules.event.annotation.DomainEvent;

@DomainEvent
public record UnassignedUserFromOffice(OfficeId officeId, UserId userId) {}
