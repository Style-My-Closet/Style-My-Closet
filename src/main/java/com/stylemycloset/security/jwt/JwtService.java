package com.stylemycloset.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.stylemycloset.security.dto.data.JwtObject;
import com.stylemycloset.user.dto.data.UserDto;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

  @Value("${security.jwt.secret}")
  private String secret;

  @Value("${security.jwt.access-token-validity-seconds}")
  private long accessTokenValiditySeconds;

  @Value("${security.jwt.refresh-token-validity-seconds}")
  private long refreshTokenValiditySeconds;

  private final JwtSessionRepository jwtSessionRepository;
  private final ObjectMapper objectMapper;

  @Transactional
  public JwtSession createJwtSession(UserDto userDto) {
    jwtSessionRepository.findByUserId(userDto.id())
        .ifPresent(this::invalidate);

    JwtObject accessJwtObject = generateJwtObject(userDto, accessTokenValiditySeconds);
    JwtObject refreshJwtObject = generateJwtObject(userDto, refreshTokenValiditySeconds);

    JwtSession jwtSession = new JwtSession(userDto.id(), accessJwtObject.token(),
        refreshJwtObject.token(), accessJwtObject.expirationTime());
    jwtSessionRepository.save(jwtSession);

    return jwtSession;

  }

  private JwtObject generateJwtObject(UserDto userDto, long tokenValiditySeconds) {
    Instant issueTime = Instant.now();
    Instant expirationTime = issueTime.plus(Duration.ofSeconds(tokenValiditySeconds));

    Map<String, Object> userDtoMap = objectMapper.convertValue(userDto, Map.class);

    JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
        .subject(userDto.name())
        .claim("userDto", userDtoMap)
        .issueTime(new Date(issueTime.toEpochMilli()))
        .expirationTime(new Date(expirationTime.toEpochMilli()))
        .build();

    JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);
    SignedJWT signedJWT = new SignedJWT(header, claimsSet);

    try {
      signedJWT.sign(new MACSigner(secret));
    } catch (JOSEException e) {
      log.error("Token signing failed: {}", e.getMessage());
      throw new IllegalArgumentException("시크릿 생성 실패", e);
    }

    String token = signedJWT.serialize();
    return new JwtObject(issueTime, expirationTime, userDto, token);
  }

  private void invalidate(JwtSession session) {
    jwtSessionRepository.delete(session);
  }

}
