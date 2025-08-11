package com.stylemycloset.user.repository;

import com.stylemycloset.user.entity.User;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom {

  boolean existsByEmail(String email);


  Set<User> findByLockedFalseAndDeleteAtIsNull();
}
