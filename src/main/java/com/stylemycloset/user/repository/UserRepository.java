package com.stylemycloset.user.repository;

import com.stylemycloset.user.entity.User;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom {
    
    Optional<User> findByName(String name);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);


  // User 엔티티에 deleteAt 컬럼이 없으므로, 테스트/리스너에서 사용 가능한 대체 메서드로 교체
  Set<User> findByLockedFalse();
}
