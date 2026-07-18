package com.lprevidente.orgcraft.team.application.query;

import com.lprevidente.orgcraft.team.application.projection.MemberView;
import com.lprevidente.orgcraft.team.application.projection.TeamMemberView;
import com.lprevidente.orgcraft.team.api.TeamId;
import com.lprevidente.orgcraft.team.domain.TeamMemberId;
import com.lprevidente.orgcraft.user.api.UserId;
import com.lprevidente.orgcraft.user.api.UserApi;
import java.util.Collection;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.jmolecules.ddd.annotation.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TeamMemberQueryService {
  private final UserApi userApi;
  private final TeamMemberReadRepository teamMembers;

  @Transactional(readOnly = true)
  public Collection<TeamMemberView> getTeamMembers(TeamId teamId) {
    final var memberships = teamMembers.findAllById_TeamId(teamId, TeamMemberView.class);

    final var userIds =
        memberships.stream()
            .map(TeamMemberView::getId)
            .map(TeamMemberId::getUserId)
            .map(UserId::id)
            .collect(Collectors.toSet());

    final var users = userApi.findAllById(userIds, MemberView.class);

    memberships.forEach(
        membership -> {
          final var userId = membership.getId().getUserId().id();
          // if (users.containsKey(userId)) membership.setUser(users.get(userId));
        });

    return memberships;
  }
}
