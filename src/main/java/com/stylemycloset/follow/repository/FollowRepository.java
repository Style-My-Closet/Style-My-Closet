package com.stylemycloset.follow.repository;

import com.stylemycloset.follow.entity.Follow;
import com.stylemycloset.follow.repository.impl.FollowRepositoryCustom;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long>, FollowRepositoryCustom {

  @Query("""
      SELECT COUNT(f)
      FROM Follow f
      WHERE f.followee.id = :userId
      AND f.followee.deletedAt IS NULL
      """)
  long countActiveFollowers(@Param("userId") Long userId);

  @Query("""
      SELECT COUNT(f)
      FROM Follow f
      WHERE f.follower.id = :userId
      AND f.follower.deletedAt IS NULL
      """)
  long countActiveFollowings(@Param("userId") Long userId);

  @Query("""
      SELECT f
      FROM Follow f
      WHERE f.followee.id = :followeeId
      AND f.follower.id = :followerId
      AND f.followee.deletedAt IS NULL
      AND f.follower.deletedAt IS NULL
      """)
  Optional<Follow> findActiveByFolloweeIdAndFollowerId(Long followeeId, Long followerId);

  @Query("""
      SELECT COUNT(f) > 0
      FROM Follow f
      WHERE f.followee.id = :followeeId
      AND f.follower.id = :followerId
      AND f.followee.deletedAt IS NULL
      AND f.follower.deletedAt IS NULL
      """)
  boolean existsActiveByFolloweeIdAndFollowerId(Long followeeId, Long followerId);

  @Query("SELECT f.follower.id "
      + "FROM Follow f "
      + "WHERE f.followee.id = :followeeId "
      + "AND f.follower.deletedAt is null")
  Set<Long> findFollowerIdsByFolloweeId(@Param("followeeId") Long followeeId);

}
