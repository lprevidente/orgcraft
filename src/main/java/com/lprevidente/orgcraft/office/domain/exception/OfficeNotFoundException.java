package com.lprevidente.orgcraft.office.domain.exception;

import com.lprevidente.orgcraft.common.exception.DomainException;
import com.lprevidente.orgcraft.office.api.OfficeId;
import org.springframework.http.HttpStatus;

public class OfficeNotFoundException extends DomainException {

  public OfficeNotFoundException(OfficeId id) {
    super(
        "Office with ID %s not found".formatted(id.id()),
        "OFFICE_NOT_FOUND",
        HttpStatus.NOT_FOUND,
        "Office Not Found");
  }
}
