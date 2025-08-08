package com.stylemycloset.follow.repository.querydsl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.stylemycloset.follow.entity.Follow;
import com.stylemycloset.follow.entity.QFollow;
import com.stylemycloset.follow.repository.querydsl.cursor.CursorStrategy;
import com.stylemycloset.follow.repository.querydsl.cursor.FollowCursorField;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;

@Slf4j
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
    CursorStrategy<?> cursorStrategy = FollowCursorField.resolveStrategy(sortBy);
    CursorStrategy<?> idAfterStrategy = FollowCursorField.resolveStrategy(
        QFollow.follow.id.getMetadata().getName()
    );

    List<Follow> follows = queryFactory
        .selectFrom(QFollow.follow)
        .join(QFollow.follow.followee).fetchJoin()
        .join(QFollow.follow.follower).fetchJoin()
        .where(
            QFollow.follow.follower.id.eq(followerId),
            buildNameLikeCondition(nameLike),
            cursorStrategy.buildPredicate(sortDirection, cursor),
            idAfterStrategy.buildPredicate(sortDirection, idAfter),
            QFollow.follow.deletedAt.isNull(),
            QFollow.follow.followee.deletedAt.isNull()
        )
        .orderBy(
            cursorStrategy.buildOrder(sortDirection, cursor),
            idAfterStrategy.buildOrder(sortDirection, idAfter)
        )
        .limit(limit + 1)
        .fetch();

    return convertToSlice(limit, follows, cursorStrategy, sortDirection);
  }

  private BooleanExpression buildNameLikeCondition(String nameLike) {
    if (nameLike != null && !nameLike.isBlank()) {
      return QFollow.follow.followee.name.containsIgnoreCase(nameLike);
    }
    return null;
  }

  private SliceImpl<Follow> convertToSlice(
      Integer limit,
      List<Follow> follows,
      CursorStrategy<?> cursorStrategy,
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
