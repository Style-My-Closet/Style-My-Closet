package com.stylemycloset.security;

import com.stylemycloset.user.dto.data.UserDto;
import com.stylemycloset.user.entity.Role;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@RequiredArgsConstructor
public class ClosetUserDetails implements UserDetails {

  private final Long userId;
  private final String email;
  private final String name;
  private final String password;
  private final Role role;

  public ClosetUserDetails(UserDto userDto, String password) {
    this.userId = userDto.id();
    this.email = userDto.email();
    this.name = userDto.name();
    this.password = password;
    this.role = userDto.role();
  }

  public ClosetUserDetails(Long userId, String roleName, String name) {
    this.userId = userId;
    this.email = null;
    this.name = name;
    this.password = null;
    this.role = Role.valueOf(roleName);
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_".concat(this.role.name())));
  }

  @Override
  public String getPassword() {
    return this.password;
  }

  @Override
  public String getUsername() {
    return this.name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ClosetUserDetails that = (ClosetUserDetails) o;
    return Objects.equals(this.userId, that.userId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.userId);
  }

}
