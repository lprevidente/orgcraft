package com.lprevidente.orgcraft.team.application.query;

import com.lprevidente.orgcraft.team.api.TeamId;
import com.lprevidente.orgcraft.team.domain.TeamMember;
import com.lprevidente.orgcraft.team.domain.TeamMemberId;
import java.util.List;
import org.springframework.data.repository.Repository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface TeamMemberReadRepository extends Repository<TeamMember, TeamMemberId> {

  <T> List<T> findAllById_TeamId(TeamId id, Class<T> projection);
}
