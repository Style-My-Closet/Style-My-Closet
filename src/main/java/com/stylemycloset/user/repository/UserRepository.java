package com.stylemycloset.user.repository;

import com.stylemycloset.user.entity.User;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom {

  boolean existsByEmail(String email);

  @Query("SELECT u.id FROM User u WHERE u.deletedAt is null")
  Set<Long> findActiveUserIds();

  Optional<User> findByEmail(String email);

  // ID로 활성화된(삭제되지 않고, 잠기지 않은) 유저만 찾는 메서드
  Optional<User> findByIdAndDeletedAtIsNullAndLockedIsFalse(Long id);

}
