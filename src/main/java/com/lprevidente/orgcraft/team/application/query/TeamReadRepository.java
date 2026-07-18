package com.lprevidente.orgcraft.team.application.query;

import com.lprevidente.orgcraft.team.domain.Team;
import com.lprevidente.orgcraft.team.api.TeamId;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface TeamReadRepository extends Repository<Team, TeamId> {

  <T> List<T> findAllBy(Class<T> projection);

  <T> List<T> findByIdIn(Collection<TeamId> ids, Class<T> projection);

  <T> Optional<T> findById(TeamId id, Class<T> projection);
}
