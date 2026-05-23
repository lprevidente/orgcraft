package com.lprevidente.orgcraft.team.domain;

import com.lprevidente.orgcraft.team.api.TeamId;
import org.jmolecules.ddd.annotation.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface Teams extends JpaRepository<Team, TeamId> {}
