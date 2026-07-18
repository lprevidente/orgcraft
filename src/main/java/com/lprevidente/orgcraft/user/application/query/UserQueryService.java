package com.lprevidente.orgcraft.user.application.query;

import com.lprevidente.orgcraft.user.application.projection.UserView;
import com.lprevidente.orgcraft.user.api.UserId;
import com.lprevidente.orgcraft.user.domain.exception.UserNotFoundException;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jmolecules.ddd.annotation.Service;

@Service
@RequiredArgsConstructor
public class UserQueryService {
  private final UserReadRepository users;

  public Collection<UserView> findAll() {
    return users.findAllBy(UserView.class);
  }

  public Collection<UserView> findAllByIds(Collection<UserId> ids) {
    if (ids.isEmpty()) {
      return List.of();
    }
    return users.findAllByIdIn(ids, UserView.class);
  }

  public UserView getUserById(UserId userId) {
    return users
        .findById(userId, UserView.class)
        .orElseThrow(() -> new UserNotFoundException(userId));
  }
}
