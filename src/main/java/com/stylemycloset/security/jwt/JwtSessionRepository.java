package com.stylemycloset.security.jwt;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JwtSessionRepository extends JpaRepository<JwtSession, Long> {

  Optional<JwtSession> findByUserId(Long userId);

  Optional<JwtSession> findByRefreshToken(String refreshToken);

}
