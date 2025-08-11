package com.stylemycloset.user.repository;

import com.stylemycloset.user.entity.User;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom {

  boolean existsByEmail(String email);

  @Query("SELECT u.id FROM User u WHERE u.deleteAt is null")
  Set<Long> findActiveUserIds();
}
