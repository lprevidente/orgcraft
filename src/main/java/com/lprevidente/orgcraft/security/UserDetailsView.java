package com.lprevidente.orgcraft.security;

import com.lprevidente.orgcraft.user.api.UserId;
import java.util.Collection;
import java.util.List;
import org.jmolecules.architecture.cqrs.QueryModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@QueryModel
public interface UserDetailsView extends UserDetails {

  UserId getId();

  String getEmail();

  @Override
  @Value("#{target.password.hashedValue()}")
  String getPassword();

  @Override
  default String getUsername() {
    return getEmail();
  }

  @Override
  default Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of();
  }
}
