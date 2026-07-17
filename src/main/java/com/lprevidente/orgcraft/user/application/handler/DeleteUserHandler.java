package com.lprevidente.orgcraft.user.application.handler;

import com.lprevidente.orgcraft.user.application.command.DeleteUser;
import com.lprevidente.orgcraft.user.domain.Users;
import com.lprevidente.orgcraft.user.domain.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.cqrs.CommandHandler;
import org.jmolecules.ddd.annotation.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteUserHandler {

  private final Users users;

  @CommandHandler
  @Transactional
  public void handle(DeleteUser command) {
    final var user =
        users
            .findById(command.id()) //
            .orElseThrow(() -> new UserNotFoundException(command.id()));
    user.delete();
    users.delete(user);
  }
}
