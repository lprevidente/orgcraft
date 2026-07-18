package com.lprevidente.orgcraft.office.application.query;

import com.lprevidente.orgcraft.office.domain.Office;
import com.lprevidente.orgcraft.office.api.OfficeId;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface OfficeReadRepository extends Repository<Office, OfficeId> {

  <T> List<T> findAllBy(Class<T> projection);

  <T> List<T> findByIdIn(Collection<OfficeId> ids, Class<T> projection);

  <T> Optional<T> findById(OfficeId id, Class<T> projection);
}
