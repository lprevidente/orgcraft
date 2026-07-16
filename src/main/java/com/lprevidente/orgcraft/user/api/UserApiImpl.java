package com.lprevidente.orgcraft.user.api;

import com.lprevidente.orgcraft.user.application.query.UserReadRepository;
import com.lprevidente.orgcraft.user.domain.Email;
import com.lprevidente.orgcraft.user.domain.Password;
import com.lprevidente.orgcraft.user.domain.User;
import com.lprevidente.orgcraft.user.domain.Users;
import lombok.RequiredArgsConstructor;
import org.jmolecules.ddd.annotation.Service;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
class UserApiImpl implements UserApi {

  private final Users users;
  private final UserReadRepository userReadRepository;

  @Override
  public boolean existsById(UUID id) {
    return users.existsById(new UserId(id));
  }

  @Override
  public <T> Optional<T> findById(UUID id, Class<T> clazz) {
    return userReadRepository.findById(new UserId(id), clazz);
  }

  @Override
  public <T> Optional<T> findByEmail(String email, Class<T> clazz) {
    return userReadRepository.findByEmail(new Email(email), clazz);
  }

  @Override
  public <T extends UserIdDto> Map<UUID, T> findAllById(Collection<UUID> ids, Class<T> clazz) {
    return userReadRepository.findAllByIdIn(ids.stream().map(UserId::new).toList(), clazz)
        .stream()
        .collect(Collectors.toMap(t -> t.getId().id(), Function.identity()));
  }

  @Override
  public void register(UserId id, String firstName, String lastName, String email, String plainPassword) {
    final var user = new User(id, firstName, lastName, Password.create(plainPassword), new Email(email), users);
    users.save(user);
  }
}
