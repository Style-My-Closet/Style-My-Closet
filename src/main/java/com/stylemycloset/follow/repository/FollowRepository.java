package com.stylemycloset.follow.repository;

import com.stylemycloset.follow.entity.Follow;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

  @Query("""
      SELECT COUNT(f) 
      FROM Follow f
      WHERE f.follower.id = :targetUserId
      """)
  long countFollowers(Long targetUserId);

  @Query("""
      SELECT COUNT(f) 
      FROM Follow f
      WHERE f.followee.id = :targetUserId
      """)
  long countFollowings(Long targetUserId);

  Optional<Follow> findByFolloweeIdAndFollowerId(Long followeeId, Long followerId);

}
