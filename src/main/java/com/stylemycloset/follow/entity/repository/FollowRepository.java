package com.stylemycloset.follow.entity.repository;

import com.stylemycloset.follow.entity.Follow;
import com.stylemycloset.user.entity.User;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FollowRepository extends JpaRepository<Follow, Long> {

  @Query("SELECT f.follower FROM Follow f WHERE f.followee.id = :followeeId")
  Set<User> findFollowersByFolloweeId(@Param("followeeId") Long followeeId);
}
