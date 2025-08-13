package com.stylemycloset.security.dto.data;

import com.stylemycloset.user.dto.data.UserDto;
import java.time.Instant;

public record JwtObject(
    Instant issueTime,
    Instant expirationTime,
    UserDto userDto,
    String token
) {

  public boolean isExpired() {
    return expirationTime.isBefore(Instant.now());
  }
}
