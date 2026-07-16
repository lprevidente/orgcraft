package com.lprevidente.orgcraft.office.domain.event;

import java.util.UUID;
import org.jmolecules.event.annotation.DomainEvent;

@DomainEvent
public record OfficeDeleted(UUID officeId) {}
