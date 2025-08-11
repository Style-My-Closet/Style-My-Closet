package com.stylemycloset.ootd.repo;

import com.stylemycloset.ootd.entity.Feed;
import com.stylemycloset.ootd.entity.FeedLike;
import com.stylemycloset.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedLikeRepository extends JpaRepository<FeedLike, Long> {

  Optional<FeedLike> findByUserAndFeed(User user, Feed feed);

  long countByFeed(Feed feed);

  boolean existsByUserAndFeed(User user, Feed feed);


}
