package com.lprevidente.orgcraft.office.domain.event;

import com.lprevidente.orgcraft.office.api.OfficeId;
import com.lprevidente.orgcraft.user.api.UserId;
import org.jmolecules.event.annotation.DomainEvent;

@DomainEvent
public record PromotedOfficeOccupantToAdmin(OfficeId officeId, UserId userId) {}
