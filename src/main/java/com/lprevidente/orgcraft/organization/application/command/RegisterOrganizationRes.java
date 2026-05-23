package com.lprevidente.orgcraft.organization.application.command;

import com.lprevidente.orgcraft.organization.api.OrganizationId;
import com.lprevidente.orgcraft.user.api.UserId;

public record RegisterOrganizationRes(OrganizationId organizationId, UserId founderId) {}
