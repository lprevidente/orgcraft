package com.lprevidente.orgcraft.organization.api;

import java.util.Optional;
import org.jmolecules.ddd.annotation.Service;

@Service
public interface OrganizationApi {

  Optional<OrganizationId> findIdBySlug(String slug);
}
