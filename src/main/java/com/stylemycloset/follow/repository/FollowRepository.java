package com.stylemycloset.follow.repository;

import com.stylemycloset.follow.entity.Follow;
import com.stylemycloset.follow.repository.querydsl.FollowRepositoryCustom;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long>, FollowRepositoryCustom {

  @Query("""
    SELECT f
    FROM Follow f
    WHERE f.followee.id = :followeeId
      AND f.follower.id = :followerId
      AND f.deletedAt IS NOT NULL
""")
  Optional<Follow> findDeletedByFolloweeIdAndFollowerId(Long followeeId, Long followerId);

  @Query("""
      SELECT COUNT(f)
      FROM Follow f
      WHERE f.follower.id = :targetUserId
      AND f.deletedAt IS NULL
      """)
  long countActiveFollowers(Long targetUserId);

  @Query("""
      SELECT COUNT(f)
      FROM Follow f
      WHERE f.followee.id = :targetUserId
       AND f.deletedAt IS NULL
      """)
  long countActiveFollowings(Long targetUserId);

  @Query("""
      SELECT f
      FROM Follow f
      WHERE f.followee.id = :followeeId
      AND f.follower.id = :followerId
      AND f.deletedAt IS NULL
      """)
  Optional<Follow> findActiveByFolloweeIdAndFollowerId(Long followeeId, Long followerId);

  @Query("""
      SELECT COUNT(f) > 0
      FROM Follow f
      WHERE f.followee.id = :followeeId
      AND f.follower.id = :followerId
      AND f.deletedAt IS NULL
           """)
  boolean existsActiveByFolloweeIdAndFollowerId(Long followeeId, Long followerId);

}
