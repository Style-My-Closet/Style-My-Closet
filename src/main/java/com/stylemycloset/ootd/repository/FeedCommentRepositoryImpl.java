package com.stylemycloset.ootd.repository;

import static com.stylemycloset.ootd.entity.QFeedComment.feedComment;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.stylemycloset.ootd.dto.CommentSearchRequest;
import com.stylemycloset.ootd.entity.FeedComment;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FeedCommentRepositoryImpl implements FeedCommentRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<FeedComment> findByFeedIdWithCursor(Long feedId, CommentSearchRequest request) {
    int limit = request.limit() != null ? request.limit() : 10;

    return queryFactory
        .selectFrom(feedComment)
        .join(feedComment.author).fetchJoin()
        .where(
            feedComment.feed.id.eq(feedId),
            cursorCondition(request)       // 커서 조건
        )
        .orderBy(feedComment.createdAt.desc(), feedComment.id.desc()) // 최신순 정렬
        .limit(limit + 1)
        .fetch();
  }

  private BooleanExpression cursorCondition(CommentSearchRequest request) {
    if (request.cursor() == null || request.idAfter() == null) {
      return null; // 첫 페이지
    }

    Instant cursorDate = Instant.parse(request.cursor());
    return feedComment.createdAt.lt(cursorDate)
        .or(feedComment.createdAt.eq(cursorDate).and(feedComment.id.lt(request.idAfter())));
  }
}
