package com.lprevidente.orgcraft.organization.domain.exception;

import com.lprevidente.orgcraft.common.exception.DomainException;
import com.lprevidente.orgcraft.organization.api.OrganizationId;
import org.springframework.http.HttpStatus;

public class OrganizationNotFoundException extends DomainException {

  public OrganizationNotFoundException(OrganizationId id) {
    super(
        "Organization with ID %s not found".formatted(id.id()),
        "ORGANIZATION_NOT_FOUND",
        HttpStatus.NOT_FOUND,
        "Organization Not Found");
  }
}
