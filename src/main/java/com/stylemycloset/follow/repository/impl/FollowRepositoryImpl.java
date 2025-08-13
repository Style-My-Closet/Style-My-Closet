package com.stylemycloset.follow.repository.impl;

import static com.stylemycloset.follow.entity.QFollow.follow;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.stylemycloset.common.repository.cursor.CursorStrategy;
import com.stylemycloset.follow.entity.Follow;
import com.stylemycloset.follow.repository.FollowRepositoryCustom;
import com.stylemycloset.follow.repository.cursor.FollowCursorField;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;

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

    return convertToSlice(limit, follows, cursorStrategy, sortDirection);
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

    return convertToSlice(limit, follows, cursorStrategy, sortDirection);
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

  private SliceImpl<Follow> convertToSlice(
      Integer limit,
      List<Follow> follows,
      CursorStrategy<?, ?> cursorStrategy,
      String sortDirection
  ) {
    Objects.requireNonNull(limit, "limit은 null이 될 수 없습니다");
    boolean hasNext = follows.size() > limit;
    List<Follow> content = follows.subList(0, Math.min(follows.size(), limit));
    Sort sort = Sort.by(
        cursorStrategy.parseDirectionOrDefault(sortDirection),
        cursorStrategy.path().getMetadata().getName()
    );

    return new SliceImpl<>(
        content,
        PageRequest.of(0, limit, sort),
        hasNext
    );
  }

}
