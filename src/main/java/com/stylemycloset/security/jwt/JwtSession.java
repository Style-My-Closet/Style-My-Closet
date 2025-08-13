package com.stylemycloset.security.jwt;

import com.stylemycloset.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "jwt_sessions")
@Entity
public class JwtSession extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "jwt_sessions_seq_gen")
  @SequenceGenerator(name = "jwt_sessions_seq_gen", sequenceName = "jwt_sessions_id_seq", allocationSize = 1)
  @Column(updatable = false, nullable = false)
  private Long Id;
  @Column(updatable = false, nullable = false)
  private Long userId;
  @Column(columnDefinition = "varchar(512)", nullable = false, unique = true)
  private String accessToken;
  @Column(columnDefinition = "varchar(512)", nullable = false, unique = true)
  private String refreshToken;
  @Column(columnDefinition = "timestamp with time zone", nullable = false)
  private Instant expirationTime;

  public JwtSession(Long userId, String accessToken, String refreshToken, Instant expirationTime) {
    this.userId = userId;
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    this.expirationTime = expirationTime;
  }

  public boolean isExpired() {
    return this.expirationTime.isBefore(Instant.now());
  }

  public void update(String accessToken, String refreshToken, Instant expirationTime) {
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    this.expirationTime = expirationTime;
  }

}
