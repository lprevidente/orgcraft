package com.lprevidente.orgcraft.team.domain.event;

import com.lprevidente.orgcraft.team.api.TeamId;
import com.lprevidente.orgcraft.user.api.UserId;

public record AddedUserToTeam(TeamId teamId, UserId userId) {
}
