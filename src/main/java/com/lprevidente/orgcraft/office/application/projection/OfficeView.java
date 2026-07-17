package com.lprevidente.orgcraft.office.application.projection;

import com.lprevidente.orgcraft.office.domain.Address;
import com.lprevidente.orgcraft.office.api.OfficeId;
import org.jmolecules.architecture.cqrs.QueryModel;

@QueryModel
public interface OfficeView {
  OfficeId getId();

  String getName();

  Address getAddress();
}
