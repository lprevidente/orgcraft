package com.lprevidente.orgcraft.team.application.query;

import com.lprevidente.orgcraft.team.domain.Team;
import com.lprevidente.orgcraft.team.api.TeamId;
import java.util.List;
import org.springframework.data.repository.Repository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface TeamReadRepository extends Repository<Team, TeamId> {

  <T> List<T> findAllBy(Class<T> projection);
}
