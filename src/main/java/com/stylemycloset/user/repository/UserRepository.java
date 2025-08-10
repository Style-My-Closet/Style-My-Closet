package com.stylemycloset.user.repository;

import com.stylemycloset.user.entity.User;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByName(String name);
    
    Optional<User> findByEmail(String email);


  Set<User> findByLockedFalseAndDeleteAtIsNull();
}
