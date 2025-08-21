package com.stylemycloset.ootd.repo;

import com.stylemycloset.ootd.entity.Feed;
import com.stylemycloset.ootd.entity.FeedLike;
import com.stylemycloset.user.entity.User;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FeedLikeRepository extends JpaRepository<FeedLike, Long> {

  Optional<FeedLike> findByUserAndFeed(User user, Feed feed);

  long countByFeed(Feed feed);

  boolean existsByUserAndFeed(User user, Feed feed);

  // Batch 쿼리로 좋아요 수 조회
  interface FeedLikeCountProjection {
    Long getFeedId();
    Long getLikeCount();
  }

  @Query("SELECT fl.feed.id as feedId, COUNT(fl) as likeCount " +
         "FROM FeedLike fl WHERE fl.feed.id IN :feedIds GROUP BY fl.feed.id")
  List<FeedLikeCountProjection> countByFeedIds(@Param("feedIds") List<Long> feedIds);

  // Batch 쿼리로 사용자가 좋아요한 피드 ID 목록 조회
  @Query("SELECT fl.feed.id FROM FeedLike fl WHERE fl.user.id = :userId AND fl.feed.id IN :feedIds")
  List<Long> findFeedIdsByUserAndFeedIds(@Param("userId") Long userId, @Param("feedIds") List<Long> feedIds);

}
