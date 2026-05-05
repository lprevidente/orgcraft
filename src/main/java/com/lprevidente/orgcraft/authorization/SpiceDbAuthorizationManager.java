package com.lprevidente.orgcraft.authorization;

import com.authzed.api.v1.CheckPermissionRequest;
import com.authzed.api.v1.CheckPermissionResponse;
import com.authzed.api.v1.ObjectReference;
import com.authzed.api.v1.PermissionsServiceGrpc.PermissionsServiceBlockingStub;
import com.authzed.api.v1.SubjectReference;
import com.lprevidente.orgcraft.security.UserDetailsView;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

@Slf4j
@Component("spiceDbAuthorizationManager")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "orgcraft.spicedb.enabled", havingValue = "true")
class SpiceDbAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

  private static final String TEAM_RESOURCE = "team";
  private static final String USER_SUBJECT = "user";
  private static final String VIEW_PERMISSION = "view";
  private static final String PATH_VARIABLE_ID = "id";

  private final PermissionsServiceBlockingStub permissionsService;

  @Override
  public AuthorizationDecision authorize(
      Supplier<? extends Authentication> authentication, RequestAuthorizationContext context) {
    final var auth = authentication.get();
    if (auth == null
        || !auth.isAuthenticated()
        || auth instanceof AnonymousAuthenticationToken
        || !(auth.getPrincipal() instanceof UserDetailsView principal)) {
      return new AuthorizationDecision(false);
    }

    final var teamId = context.getVariables().get(PATH_VARIABLE_ID);
    if (teamId == null) return new AuthorizationDecision(false);

    final var request =
        CheckPermissionRequest.newBuilder()
            .setResource(
                ObjectReference.newBuilder().setObjectType(TEAM_RESOURCE).setObjectId(teamId))
            .setPermission(VIEW_PERMISSION)
            .setSubject(
                SubjectReference.newBuilder()
                    .setObject(
                        ObjectReference.newBuilder()
                            .setObjectType(USER_SUBJECT)
                            .setObjectId(principal.getId().id().toString())))
            .build();

    final var response = permissionsService.checkPermission(request);
    final var permitted =
        response.getPermissionship()
            == CheckPermissionResponse.Permissionship.PERMISSIONSHIP_HAS_PERMISSION;
    log.debug(
        "SpiceDB check team:{}#{}@user:{} -> {}",
        teamId,
        VIEW_PERMISSION,
        principal.getId().id(),
        permitted);
    return new AuthorizationDecision(permitted);
  }
}
