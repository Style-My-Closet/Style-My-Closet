package com.stylemycloset.user.repository;

import com.stylemycloset.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

}
