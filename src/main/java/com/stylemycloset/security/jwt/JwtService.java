package com.stylemycloset.security.jwt;

import org.springframework.beans.factory.annotation.Value;

public class JwtService {

  @Value("${security.jwt.secret}")
  private String secret;

  @Value("${security.jwt.access-token-validity-seconds}")
  private long accessTokenValiditySeconds;

  @Value("${security.jwt.refresh-token-validity-seconds}")
  private long refreshTokenValiditySeconds;

}
