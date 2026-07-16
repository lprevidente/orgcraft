package com.lprevidente.orgcraft.office.domain.event;

import com.lprevidente.orgcraft.organization.api.OrganizationId;
import com.lprevidente.orgcraft.user.api.UserId;
import java.util.UUID;
import org.jmolecules.event.annotation.DomainEvent;

@DomainEvent
public record OfficeCreated(UUID officeId, UserId creator, OrganizationId organization) {}
