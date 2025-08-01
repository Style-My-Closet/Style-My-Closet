package com.stylemycloset.notification.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.stylemycloset.notification.dto.NotificationFindAllRequest;
import com.stylemycloset.notification.entity.Notification;
import com.stylemycloset.notification.entity.QNotification;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class NotificationQueryRepository {

  private final JPAQueryFactory queryFactory;
  private final QNotification qNotification = QNotification.notification;

  public List<Notification> findAllByCursor(NotificationFindAllRequest request, long receiverId) {
    BooleanBuilder builder = new BooleanBuilder();

    builder.and(qNotification.receiver.id.eq(receiverId));

    if(request.cursor() != null && request.idAfter() != null) {
      Instant cursor = request.cursor();
      builder.and(
          qNotification.createdAt.before(cursor)
              .or(qNotification.createdAt.eq(cursor)
                  .and(qNotification.id.lt(request.idAfter()))
              )
      );
    }

    return queryFactory
        .selectFrom(qNotification)
        .where(builder)
        .orderBy(
            qNotification.createdAt.desc(),
            qNotification.id.desc())
        .limit(request.limit() + 1)
        .fetch();
  }
}
