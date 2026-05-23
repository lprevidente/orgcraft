package com.lprevidente.orgcraft.organization.domain.event;

import com.lprevidente.orgcraft.organization.api.OrganizationId;
import org.jmolecules.event.annotation.DomainEvent;

@DomainEvent
public record OrganizationCreated(OrganizationId id) {}
