package com.lprevidente.orgcraft.user.infrastructure.rest;

import com.lprevidente.orgcraft.common.authorization.ResourceAuthorization;
import com.lprevidente.orgcraft.common.authorization.SpiceDbSchema.Permission;
import com.lprevidente.orgcraft.common.authorization.SpiceDbSchema.Type;
import com.lprevidente.orgcraft.user.api.UserId;
import com.lprevidente.orgcraft.user.application.command.CreateUserReq;
import com.lprevidente.orgcraft.user.application.command.CreateUserRes;
import com.lprevidente.orgcraft.user.application.command.DeleteUser;
import com.lprevidente.orgcraft.user.application.command.UpdateUser;
import com.lprevidente.orgcraft.user.application.handler.AddUserHandler;
import com.lprevidente.orgcraft.user.application.handler.DeleteUserHandler;
import com.lprevidente.orgcraft.user.application.handler.UpdateUserHandler;
import com.lprevidente.orgcraft.user.application.projection.UserView;
import com.lprevidente.orgcraft.user.application.query.UserQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
class UserController {
  private final UserQueryService userQueryService;
  private final AddUserHandler addUserHandler;
  private final UpdateUserHandler updateUserHandler;
  private final DeleteUserHandler deleteUserHandler;
  private final ResourceAuthorization authorization;

  @GetMapping
  Collection<UserView> getUsers(@AuthenticationPrincipal(expression = "id") UserId requester) {
    return authorization.accessibleResourceIds(Type.USER, Permission.VIEW, requester.id())
        .map(ids -> ids.stream().map(UserId::new).collect(Collectors.toSet()))
        .map(userQueryService::findAllByIds)
        .orElseGet(userQueryService::findAll);
  }

  @GetMapping("{id}")
  @PreAuthorize("hasPermission(#id, 'user', 'view')")
  UserView getUser(@PathVariable UserId id) {
    return userQueryService.getUserById(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasPermission(@tenant.organizationId(), 'organization', 'manage')")
  CreateUserRes createUser(@RequestBody @Valid CreateUserReq command) {
    return addUserHandler.handle(command);
  }

  @PutMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void updateUser(@RequestBody @Valid UpdateUser command) {
    updateUserHandler.handle(command);
  }

  @DeleteMapping("{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasPermission(@tenant.organizationId(), 'organization', 'manage')")
  void deleteUser(@PathVariable UserId id) {
    deleteUserHandler.handle(new DeleteUser(id));
  }
}
