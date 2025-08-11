package com.stylemycloset.ootd.repo;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.stylemycloset.ootd.entity.Feed;
import com.stylemycloset.ootd.entity.QFeed;
import com.stylemycloset.weather.entity.Weather.SkyStatus;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@RequiredArgsConstructor
public class FeedRepositoryImpl implements FeedRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<Feed> findByConditions(Long cursorId, String keywordLike, SkyStatus skyStatus,
      Long authorId, Pageable pageable) {
    QFeed feed = QFeed.feed;

    return queryFactory
        .selectFrom(feed)
        .join(feed.author).fetchJoin()
        .leftJoin(feed.weather).fetchJoin()
        .where(
            ltCursorId(cursorId),
            containsKeyword(keywordLike),
            eqSkyStatus(skyStatus),
            eqAuthorId(authorId)
        )
        .orderBy(feed.id.desc())
        .limit(pageable.getPageSize() + 1)
        .fetch();
  }

  // 동적 쿼리를 위한 private 메서드
  private BooleanExpression ltCursorId(Long cursorId) {
    return cursorId != null ? QFeed.feed.id.lt(cursorId) : null;
  }

  private BooleanExpression containsKeyword(String keyword) {
    return StringUtils.hasText(keyword) ? QFeed.feed.content.containsIgnoreCase(keyword) : null;
  }

  private BooleanExpression eqSkyStatus(SkyStatus skyStatus) {
    return skyStatus != null ? QFeed.feed.weather.skyStatus.eq(skyStatus) : null;
  }

  private BooleanExpression eqAuthorId(Long authorId) {
    return authorId != null ? QFeed.feed.author.id.eq(authorId) : null;
  }
}
