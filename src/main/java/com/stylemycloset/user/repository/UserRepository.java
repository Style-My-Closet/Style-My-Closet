package com.stylemycloset.user.repository;

import com.stylemycloset.user.entity.User;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom {

  boolean existsByEmail(String email);

  Optional<User> findByemail(String email);

  Set<User> findByLockedFalseAndDeleteAtIsNull();

  Optional<User> findByEmail(String email);

  // ID로 활성화된(삭제되지 않고, 잠기지 않은) 유저만 찾는 메서드
  Optional<User> findByIdAndDeleteAtIsNullAndLockedIsFalse(Long id);
}
