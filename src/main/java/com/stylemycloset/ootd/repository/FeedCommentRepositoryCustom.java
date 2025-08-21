package com.stylemycloset.ootd.repository;

import com.stylemycloset.ootd.dto.CommentSearchRequest;
import com.stylemycloset.ootd.entity.FeedComment;
import java.util.List;

public interface FeedCommentRepositoryCustom {

  List<FeedComment> findByFeedIdWithCursor(Long feedId, CommentSearchRequest request);

}
