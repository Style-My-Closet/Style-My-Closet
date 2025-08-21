package com.stylemycloset.follow.repository.impl;

import static com.stylemycloset.follow.entity.QFollow.follow;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.stylemycloset.common.repository.CursorStrategy;
import com.stylemycloset.common.repository.CustomSliceImpl;
import com.stylemycloset.follow.entity.Follow;
import com.stylemycloset.follow.repository.cursor.FollowCursorField;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;

@RequiredArgsConstructor
public class FollowRepositoryImpl implements FollowRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public Slice<Follow> findFollowingsByFollowerId(
      Long followerId,
      String cursor,
      String idAfter,
      Integer limit,
      String nameLike,
      String sortBy,
      String sortDirection
  ) {
    CursorStrategy<?, Follow> cursorStrategy = FollowCursorField.resolveStrategy(sortBy);
    CursorStrategy<?, Follow> idAfterStrategy = FollowCursorField.resolveStrategy(
        follow.id.getMetadata().getName()
    );

    List<Follow> follows = queryFactory
        .selectFrom(follow)
        .join(follow.followee).fetchJoin()
        .join(follow.follower).fetchJoin()
        .where(
            follow.follower.id.eq(followerId),
            buildFolloweeNameLikeCondition(nameLike),
            buildPredicate(cursor, idAfter, sortDirection, cursorStrategy, idAfterStrategy),
            follow.deletedAt.isNull(),
            follow.followee.deletedAt.isNull()
        )
        .orderBy(
            cursorStrategy.buildOrder(sortDirection, cursor),
            idAfterStrategy.buildOrder(sortDirection, idAfter)
        )
        .limit(limit + 1)
        .fetch();

    return CustomSliceImpl.of(follows, limit, cursorStrategy, sortDirection);
  }

  @Override
  public Slice<Follow> findFollowersByFolloweeId(
      Long followerId,
      String cursor,
      String idAfter,
      Integer limit,
      String nameLike,
      String sortBy,
      String sortDirection
  ) {
    CursorStrategy<?, Follow> cursorStrategy = FollowCursorField.resolveStrategy(sortBy);
    CursorStrategy<?, Follow> idAfterStrategy = FollowCursorField.resolveStrategy(
        follow.id.getMetadata().getName()
    );

    List<Follow> follows = queryFactory
        .selectFrom(follow)
        .join(follow.followee).fetchJoin()
        .join(follow.follower).fetchJoin()
        .where(
            follow.followee.id.eq(followerId),
            buildFollowerNameLikeCondition(nameLike),
            buildPredicate(cursor, idAfter, sortDirection, cursorStrategy, idAfterStrategy),
            follow.deletedAt.isNull(),
            follow.follower.deletedAt.isNull()
        )
        .orderBy(
            cursorStrategy.buildOrder(sortDirection, cursor),
            idAfterStrategy.buildOrder(sortDirection, idAfter)
        )
        .limit(limit + 1)
        .fetch();

    return CustomSliceImpl.of(follows, limit, cursorStrategy, sortDirection);
  }

  private BooleanExpression buildPredicate(
      String cursor,
      String idAfter,
      String sortDirection,
      CursorStrategy<?, Follow> cursorStrategy,
      CursorStrategy<?, Follow> idAfterStrategy
  ) {
    BooleanExpression booleanExpression = cursorStrategy.buildInequalityPredicate(sortDirection,
        cursor);
    BooleanExpression buildEq = cursorStrategy.buildEq(cursor);
    BooleanExpression buildSecondary = idAfterStrategy.buildInequalityPredicate(sortDirection,
        idAfter);
    if (buildEq != null && buildSecondary != null) {
      booleanExpression.or(buildEq.and(buildSecondary));
    }
    return booleanExpression;
  }

  private BooleanExpression buildFollowerNameLikeCondition(String nameLike) {
    if (nameLike != null && !nameLike.isBlank()) {
      return follow.follower.name.containsIgnoreCase(nameLike);
    }
    return null;
  }

  private BooleanExpression buildFolloweeNameLikeCondition(String nameLike) {
    if (nameLike != null && !nameLike.isBlank()) {
      return follow.followee.name.containsIgnoreCase(nameLike);
    }
    return null;
  }

}
