package com.stylemycloset.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.stylemycloset.common.exception.ErrorCode;
import com.stylemycloset.common.exception.StyleMyClosetException;
import com.stylemycloset.security.dto.data.JwtObject;
import com.stylemycloset.security.dto.data.TokenInfo;
import com.stylemycloset.user.dto.data.UserDto;
import com.stylemycloset.user.entity.Role;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
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

  public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";

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

    JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
        .subject(userDto.name())
        .claim("userId", userDto.id())
        .claim("role", userDto.role())
        .claim("name", userDto.name())
        .issueTime(new Date(issueTime.toEpochMilli()))
        .expirationTime(new Date(expirationTime.toEpochMilli()))
        .build();

    JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);
    SignedJWT signedJWT = new SignedJWT(header, claimsSet);

    try {
      signedJWT.sign(new MACSigner(secret));
    } catch (JOSEException e) {
      log.error("Token signing failed: {}", e.getMessage());
      throw new StyleMyClosetException(ErrorCode.INVALID_TOKEN_SECRET, Collections.emptyMap());
    }

    String token = signedJWT.serialize();
    return new JwtObject(issueTime, expirationTime, userDto, token);
  }

  public JwtSession getJwtSession(String refreshToken) {
    return jwtSessionRepository.findByRefreshToken(refreshToken)
        .orElseThrow(() -> new StyleMyClosetException(ErrorCode.TOKEN_NOT_FOUND,
            Map.of("refreshToken", refreshToken)));
  }

  private void invalidate(JwtSession session) {
    jwtSessionRepository.delete(session);
  }

  @Transactional
  public void invalidateJwtSession(Long userId) {
    jwtSessionRepository.findByUserId(userId)
        .ifPresent(this::invalidate);
  }

  @Transactional
  public void invalidateJwtSession(String refreshToken) {
    jwtSessionRepository.findByRefreshToken(refreshToken)
        .ifPresent(this::invalidate);
  }

  public boolean validate(String token) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(token);

      JWSVerifier verifier = new MACVerifier(secret);
      if (!signedJWT.verify(verifier)) {
        log.warn("유효하지 않은 JWT 서명입니다.");
        return false;
      }

      JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
      Date expirationTime = claims.getExpirationTime();

      if (expirationTime == null || expirationTime.before(new Date())) {
        log.warn("만료된 JWT 토큰입니다.");
        return false;
      }
      return true;

    } catch (JOSEException | ParseException e) {
      log.error("JWT 검증 실패: {}", e.getMessage());
      return false;
    }
  }

  public TokenInfo parse(String token) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(token);
      JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

      Long userId = claims.getLongClaim("userId");
      String role = claims.getStringClaim("role");
      String name = claims.getStringClaim("name");

      if (userId == null || role == null) {
        throw new StyleMyClosetException(ErrorCode.INVALID_TOKEN, Collections.emptyMap());
      }

      return new TokenInfo(userId, role, name);
    } catch (ParseException e) {
      log.error(e.getMessage());
      throw new StyleMyClosetException(ErrorCode.INVALID_TOKEN, Map.of("token", token));
    }

  }

}
