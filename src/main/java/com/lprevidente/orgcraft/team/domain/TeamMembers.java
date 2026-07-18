package com.lprevidente.orgcraft.team.domain;

import com.lprevidente.orgcraft.user.api.UserId;
import java.util.List;
import org.jmolecules.ddd.annotation.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface TeamMembers extends JpaRepository<TeamMember, TeamMemberId> {

  List<TeamMember> findByIdUserId(UserId userId);
}
