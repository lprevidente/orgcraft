package com.lprevidente.orgcraft.office.domain.event;

import com.lprevidente.orgcraft.office.api.OfficeId;
import org.jmolecules.event.annotation.DomainEvent;

@DomainEvent
public record OfficeDeleted(OfficeId officeId) {}
