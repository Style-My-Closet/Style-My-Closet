package com.stylemycloset.notification.util;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;

import com.stylemycloset.notification.entity.Notification;
import com.stylemycloset.notification.repository.NotificationRepository;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.test.util.ReflectionTestUtils;

public class NotificationStubHelper {

  private static final AtomicLong idGenerator = new AtomicLong(1L);

  public static void stubSave(NotificationRepository notificationRepository) {
    given(notificationRepository.save(any(Notification.class)))
        .willAnswer(invocation -> {
          Notification n = invocation.getArgument(0);
          ReflectionTestUtils.setField(n, "id", 1L);
          ReflectionTestUtils.setField(n, "createdAt", Instant.now());
          return n;
        });
  }

  public static void stubSaveAll(NotificationRepository notificationRepository) {
    given(notificationRepository.saveAll(anyList()))
        .willAnswer(invocation -> {
          List<Notification> notifications = invocation.getArgument(0);
          Instant createdAt = Instant.now();
          for (Notification notification : notifications) {
            ReflectionTestUtils.setField(notification, "id", idGenerator.getAndIncrement());
            ReflectionTestUtils.setField(notification, "createdAt", createdAt);
          }
          return notifications;
        });
  }
}
