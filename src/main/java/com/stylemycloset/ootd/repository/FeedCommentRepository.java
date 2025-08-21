package com.stylemycloset.ootd.repository;

import com.stylemycloset.ootd.entity.Feed;
import com.stylemycloset.ootd.entity.FeedComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedCommentRepository extends JpaRepository<FeedComment, Long>, FeedCommentRepositoryCustom {

  Long feed(Feed feed);
}
