package com.lprevidente.orgcraft.user.domain.event;

import com.lprevidente.orgcraft.user.api.UserId;
import java.util.UUID;
import org.jmolecules.event.annotation.DomainEvent;

/**
 * A user joined an organization (its tenant). Carries the organization id as a raw {@link UUID} — the
 * {@code user} module must not depend on {@code organization.api} (that would cycle).
 */
@DomainEvent
public record UserRegistered(UUID organizationId, UserId userId) {}
