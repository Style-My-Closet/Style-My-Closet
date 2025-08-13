package com.stylemycloset.directmessage.repository.impl;

import static com.stylemycloset.directmessage.entity.QDirectMessage.directMessage;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.stylemycloset.common.repository.cursor.CursorStrategy;
import com.stylemycloset.directmessage.entity.DirectMessage;
import com.stylemycloset.directmessage.repository.DirectMessageRepositoryCustom;
import com.stylemycloset.directmessage.repository.cursor.DirectMessageField;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;

@RequiredArgsConstructor
public class DirectMessageRepositoryImpl implements DirectMessageRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public Slice<DirectMessage> findMessagesBetweenParticipants(
      Long senderId,
      Long receiverId,
      String cursor,
      String idAfter,
      Integer limit,
      String sortBy,
      String sortDirection
  ) {
    CursorStrategy<?, DirectMessage> cursorStrategy = DirectMessageField.resolveStrategy(
        sortBy);
    CursorStrategy<?, DirectMessage> idAfterStrategy = DirectMessageField.resolveStrategy(
        directMessage.id.getMetadata().getName()
    );

    List<DirectMessage> directMessages = queryFactory
        .selectFrom(directMessage)
        .join(directMessage.sender).fetchJoin()
        .join(directMessage.receiver).fetchJoin()
        .where(
            buildConversationCondition(senderId, receiverId),
            buildPredicate(cursor, idAfter, sortDirection, cursorStrategy, idAfterStrategy),
            directMessage.deletedAt.isNull(),
            directMessage.sender.deletedAt.isNull(),
            directMessage.receiver.deletedAt.isNull()
        )
        .orderBy(
            cursorStrategy.buildOrder(sortDirection, cursor),
            idAfterStrategy.buildOrder(sortDirection, idAfter)
        )
        .limit(limit + 1)
        .fetch();

    return convertToSlice(limit, directMessages, cursorStrategy, sortDirection);
  }

  private BooleanExpression buildPredicate( // 추출 예정
      String cursor,
      String idAfter,
      String sortDirection,
      CursorStrategy<?, DirectMessage> cursorStrategy,
      CursorStrategy<?, DirectMessage> idAfterStrategy
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

  private BooleanExpression buildConversationCondition(
      Long senderId,
      Long receiverId
  ) {
    if (senderId == null || receiverId == null) {
      return null;
    }
    return directMessage.sender.id.eq(senderId).and(directMessage.receiver.id.eq(receiverId))
        .or(directMessage.sender.id.eq(receiverId).and(directMessage.receiver.id.eq(senderId)));
  }

  private SliceImpl<DirectMessage> convertToSlice(
      Integer limit,
      List<DirectMessage> directMessages,
      CursorStrategy<?, DirectMessage> cursorStrategy,
      String sortDirection
  ) {
    Objects.requireNonNull(limit, "limit은 null이 될 수 없습니다");
    boolean hasNext = directMessages.size() > limit;
    List<DirectMessage> contents = directMessages.subList(0,
        Math.min(directMessages.size(), limit));
    Sort sort = Sort.by(
        cursorStrategy.parseDirectionOrDefault(sortDirection),
        cursorStrategy.path().getMetadata().getName()
    );

    return new SliceImpl<>(
        contents,
        PageRequest.of(0, limit, sort),
        hasNext
    );
  }

}
