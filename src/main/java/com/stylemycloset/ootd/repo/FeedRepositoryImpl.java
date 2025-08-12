package com.stylemycloset.ootd.repo;

import static com.stylemycloset.ootd.entity.QFeed.feed;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.stylemycloset.ootd.dto.FeedSearchRequest;
import com.stylemycloset.ootd.entity.Feed;
import com.stylemycloset.ootd.entity.QFeed;
import com.stylemycloset.ootd.tempEnum.PrecipitationType;
import com.stylemycloset.weather.entity.Weather.SkyStatus;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@RequiredArgsConstructor
public class FeedRepositoryImpl implements FeedRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<Feed> findByConditions(FeedSearchRequest request) {
    int limit = request.limit() != null ? request.limit() : 10;

    return queryFactory
        .selectFrom(feed)
        .leftJoin(feed.author).fetchJoin()
        .leftJoin(feed.weather).fetchJoin()
        .where(
            cursorCondition(request),
            containsKeyword(request.keywordLike()),
            eqSkyStatus(request.skyStatusEqual()),
            eqPrecipitationType(request.precipitationTypeEqual()),
            eqAuthorId(request.authorIdEqual())
        )
        .orderBy(createOrderSpecifiers(request))
        .limit(limit + 1) // +1 조회
        .fetch();
  }

  private OrderSpecifier<?>[] createOrderSpecifiers(FeedSearchRequest request) {
    List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

    String sortBy = request.sortBy() != null ? request.sortBy() : "createdAt";
    Order direction =
        "ASCENDING".equalsIgnoreCase(request.sortDirection()) ? Order.ASC : Order.DESC;

    switch (sortBy) {
      case "likeCount":
        orderSpecifiers.add(new OrderSpecifier<>(direction, feed.feedLikes.size()));
        break;
      case "createdAt":
      default:
        orderSpecifiers.add(new OrderSpecifier<>(direction, feed.createdAt));
        break;
    }

    // 커서 페이징의 일관성을 위해 항상 ID로 2차 정렬
    orderSpecifiers.add(new OrderSpecifier<>(direction, feed.id));

    return orderSpecifiers.toArray(new OrderSpecifier[0]);
  }

  // 다양한 정렬 기준을 지원하는 커서 조건 메서드
  private BooleanExpression cursorCondition(FeedSearchRequest request) {
    String sortBy = request.sortBy() != null ? request.sortBy() : "createdAt";

    if (request.cursor() == null || request.idAfter() == null) {
      return null; // 첫 페이지 조회 시에는 커서 조건 없음
    }

    if ("createdAt".equals(sortBy)) {
      Instant cursorDate = Instant.parse(request.cursor());
      if ("ASCENDING".equalsIgnoreCase(request.sortDirection())) {
        return feed.createdAt.gt(cursorDate)
            .or(feed.createdAt.eq(cursorDate).and(feed.id.gt(request.idAfter())));
      } else {
        return feed.createdAt.lt(cursorDate)
            .or(feed.createdAt.eq(cursorDate).and(feed.id.lt(request.idAfter())));
      }
    }

    if ("likeCount".equals(sortBy)) {
      int likeCount = Integer.parseInt(request.cursor());
      if ("ASCENDING".equalsIgnoreCase(request.sortDirection())) {
        return feed.feedLikes.size().gt(likeCount)
            .or(feed.feedLikes.size().eq(likeCount).and(feed.id.gt(request.idAfter())));
      } else {
        return feed.feedLikes.size().lt(likeCount)
            .or(feed.feedLikes.size().eq(likeCount).and(feed.id.lt(request.idAfter())));
      }
    }

    return null;
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

  private BooleanExpression eqPrecipitationType(PrecipitationType type) {
    if (type == null) {
      return null;
    }
    return feed.weather.precipitation.type.eq(type.name());
  }
}
