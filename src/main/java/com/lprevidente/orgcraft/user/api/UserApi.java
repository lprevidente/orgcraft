package com.lprevidente.orgcraft.user.api;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.jmolecules.ddd.annotation.Service;

@Service
public interface UserApi {

  boolean existsById(UUID id);

  <T> Optional<T> findById(UUID id, Class<T> clazz);

  <T> Optional<T> findByEmail(String email, Class<T> clazz);

  <T extends UserIdDto> Map<UUID, T> findAllById(Collection<UUID> ids, Class<T> clazz);

  void register(UserId id, String firstName, String lastName, String email, String plainPassword);
}
