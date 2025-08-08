package com.stylemycloset.user.repository;

import com.stylemycloset.user.entity.User;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom {

  boolean existsByEmail(String email);


  Set<User> findByLockedFalseAndDeletedAtIsNull();
}
