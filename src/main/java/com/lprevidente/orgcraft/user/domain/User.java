package com.lprevidente.orgcraft.user.domain;

import com.lprevidente.orgcraft.user.api.UserId;
import com.lprevidente.orgcraft.user.domain.exception.EmailAlreadyInUseException;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.TenantId;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;
import org.jspecify.annotations.Nullable;
import org.springframework.util.Assert;

@Getter
@AggregateRoot
@Table(
    name = "users",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_users_tenant_email",
            columnNames = {"tenant_id", "email"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

  @Identity private UserId id;

  private String firstName;
  private String lastName;

  @AttributeOverride(name = "hashedValue", column = @Column(name = "password"))
  private Password password;

  @AttributeOverride(name = "value", column = @Column(name = "email"))
  private Email email;

  @Nullable
  @TenantId
  @Column(name = "tenant_id", nullable = false, updatable = false)
  private String tenantId;

  public User(String firstName, String lastName, Password password, Email email, Users users) {
    this(new UserId(), firstName, lastName, password, email, users);
  }

  public User(
      UserId id, String firstName, String lastName, Password password, Email email, Users users) {
    Assert.notNull(id, "id must not be null");
    Assert.notNull(firstName, "firstName must not be null");
    Assert.notNull(lastName, "lastName must not be null");
    Assert.notNull(email, "email must not be null");
    Assert.notNull(password, "password must not be null");
    if (users.existsByEmail(email)) throw new EmailAlreadyInUseException(email);

    this.id = id;
    this.firstName = firstName;
    this.lastName = lastName;
    this.password = password;
    this.email = email;
  }

  public void updateDetails(String firstName, String lastName) {
    this.firstName = firstName;
    this.lastName = lastName;
  }

  @Override
  public final boolean equals(Object o) {
    if (!(o instanceof User user)) return false;
    return Objects.equals(id, user.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
