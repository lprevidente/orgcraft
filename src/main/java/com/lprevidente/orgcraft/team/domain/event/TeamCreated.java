package com.lprevidente.orgcraft.team.domain.event;

import com.lprevidente.orgcraft.organization.api.OrganizationId;
import com.lprevidente.orgcraft.team.api.TeamId;
import com.lprevidente.orgcraft.user.api.UserId;
import org.jmolecules.event.annotation.DomainEvent;

@DomainEvent
public record TeamCreated(TeamId id, UserId creator, OrganizationId organization) {}
