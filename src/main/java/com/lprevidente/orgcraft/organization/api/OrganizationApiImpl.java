package com.lprevidente.orgcraft.organization.api;

import com.lprevidente.orgcraft.organization.domain.Organization;
import com.lprevidente.orgcraft.organization.domain.Organizations;
import com.lprevidente.orgcraft.organization.domain.Slug;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jmolecules.ddd.annotation.Service;

@Service
@RequiredArgsConstructor
class OrganizationApiImpl implements OrganizationApi {

  private final Organizations organizations;

  @Override
  public Optional<OrganizationId> findIdBySlug(String slug) {
    final Slug slugVo;
    try {
      slugVo = new Slug(slug);
    } catch (IllegalArgumentException invalidSlug) {
      return Optional.empty();
    }
    return organizations.findBySlug(slugVo).map(Organization::getId);
  }
}
