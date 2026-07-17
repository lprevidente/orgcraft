package com.lprevidente.orgcraft.organization.domain.event;

import com.lprevidente.orgcraft.organization.api.OrganizationId;
import org.jmolecules.event.annotation.DomainEvent;

/**
 * An organization was deleted. Consumers cascade-delete everything under its tenant: the team and
 * office modules wipe their aggregates (each raising its own delete event so SpiceDB resources are
 * cleaned up), and the authorization module wipes the {@code organization:{id}} tuples.
 */
@DomainEvent
public record OrganizationDeleted(OrganizationId id) {}
