package com.stylemycloset.directmessage.repository.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.stylemycloset.directmessage.entity.DirectMessage;
import com.stylemycloset.directmessage.entity.QDirectMessage;
import com.stylemycloset.directmessage.repository.DirectMessageRepositoryCustom;
import com.stylemycloset.directmessage.repository.cursor.CursorStrategy;
import com.stylemycloset.directmessage.repository.cursor.strategy.DirectMessageField;
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
    QDirectMessage directMessage = QDirectMessage.directMessage;
    CursorStrategy<?> cursorStrategy = DirectMessageField.resolveStrategy(sortBy);
    CursorStrategy<?> idAfterStrategy = DirectMessageField.resolveStrategy(
        directMessage.id.getMetadata().getName()
    );

    List<DirectMessage> directMessages = queryFactory
        .selectFrom(QDirectMessage.directMessage)
        .join(QDirectMessage.directMessage.sender).fetchJoin()
        .join(QDirectMessage.directMessage.receiver).fetchJoin()
        .where(
            buildConversationCondition(senderId, receiverId, directMessage),
            cursorStrategy.buildPredicate(sortDirection, cursor),
            idAfterStrategy.buildPredicate(sortDirection, idAfter),
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

  private BooleanExpression buildConversationCondition(
      Long senderId,
      Long receiverId,
      QDirectMessage dm
  ) {
    if (senderId == null || receiverId == null) {
      return null;
    }

    return dm.sender.id.eq(senderId).and(dm.receiver.id.eq(receiverId))
        .or(dm.sender.id.eq(receiverId).and(dm.receiver.id.eq(senderId)));
  }

  private SliceImpl<DirectMessage> convertToSlice(
      Integer limit,
      List<DirectMessage> directMessages,
      CursorStrategy<?> cursorStrategy,
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
