package com.stylemycloset.directmessage.repository.impl;

import static com.stylemycloset.directmessage.entity.QDirectMessage.directMessage;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.stylemycloset.common.repository.CursorStrategy;
import com.stylemycloset.common.repository.CustomSliceImpl;
import com.stylemycloset.directmessage.entity.DirectMessage;
import com.stylemycloset.directmessage.repository.cursor.DirectMessageField;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort.Direction;

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
      Direction direction
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
            cursorStrategy.buildCursorPredicate(direction, cursor, idAfter, idAfterStrategy),
            directMessage.deletedAt.isNull(),
            directMessage.sender.deletedAt.isNull(),
            directMessage.receiver.deletedAt.isNull()
        )
        .orderBy(
            cursorStrategy.buildOrder(direction),
            idAfterStrategy.buildOrder(direction)
        )
        .limit(limit + 1)
        .fetch();

    return CustomSliceImpl.of(
        directMessages,
        limit,
        cursorStrategy,
        direction
    );
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

}
