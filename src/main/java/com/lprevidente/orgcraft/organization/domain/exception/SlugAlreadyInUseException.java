package com.lprevidente.orgcraft.organization.domain.exception;

import com.lprevidente.orgcraft.common.exception.DomainException;
import com.lprevidente.orgcraft.organization.domain.Slug;
import org.springframework.http.HttpStatus;

/** Exception thrown when attempting to register an organization with a slug that is already taken. */
public class SlugAlreadyInUseException extends DomainException {

  public SlugAlreadyInUseException(Slug slug) {
    super(
        "Organization slug '%s' is already in use".formatted(slug),
        "SLUG_ALREADY_IN_USE",
        HttpStatus.CONFLICT,
        "Slug Already In Use");
  }
}
