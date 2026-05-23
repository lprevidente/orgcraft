package com.lprevidente.orgcraft.organization.domain;

import com.lprevidente.orgcraft.organization.api.OrganizationId;
import java.util.Optional;
import org.jmolecules.ddd.annotation.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface Organizations extends JpaRepository<Organization, OrganizationId> {

  boolean existsBySlug(Slug slug);

  Optional<Organization> findBySlug(Slug slug);
}
