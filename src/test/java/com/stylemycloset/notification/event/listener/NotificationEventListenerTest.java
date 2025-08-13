package com.stylemycloset.notification.event.listener;

import com.stylemycloset.notification.dto.NotificationDto;
import com.stylemycloset.notification.entity.NotificationLevel;
import com.stylemycloset.notification.event.domain.ClothAttributeChangedEvent;
import com.stylemycloset.notification.event.domain.NewClothAttributeEvent;
import com.stylemycloset.notification.service.NotificationService;
import com.stylemycloset.sse.service.SseService;
import com.stylemycloset.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.Set;

import static org.mockito.Mockito.*;

class NotificationEventListenerTest {

  @Mock private NotificationService notificationService;
  @Mock private UserRepository userRepository;
  @Mock private SseService sseService;

  @InjectMocks private NewClothAttributeNotificationEventListener newAttrListener;
  @InjectMocks private ClothAttributeChangedNotificationEventListener changedAttrListener;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void handleNewClothAttributeEvent_publishesNotifications_and_sendsSse() {
    // given
    when(userRepository.findActiveUserIds()).thenReturn(Set.of(1L));
    NotificationDto dto = NotificationDto.builder()
        .id(1L).createdAt(Instant.now()).receiverId(1L)
        .title("t").content("c").level(NotificationLevel.INFO).build();
    when(notificationService.createAll(anySet(), anyString(), anyString(), any())).thenReturn(java.util.List.of(dto));

    NewClothAttributeEvent event = new NewClothAttributeEvent(10L, "재질");

    // when
    newAttrListener.handler(event);

    // then
        verify(userRepository, times(1)).findActiveUserIds();
    verify(notificationService, times(1)).createAll(anySet(), contains("새로운 의상 속성"), contains("재질"), eq(NotificationLevel.INFO));
    verify(sseService, times(1)).sendNotification(any());
  }

  @Test
  void handleClothAttributeChangedEvent_publishesNotifications_and_sendsSse() {
    // given
    when(userRepository.findActiveUserIds()).thenReturn(Set.of(1L));
    NotificationDto dto = NotificationDto.builder()
        .id(2L).createdAt(Instant.now()).receiverId(1L)
        .title("t").content("c").level(NotificationLevel.INFO).build();
    when(notificationService.createAll(anySet(), anyString(), anyString(), any())).thenReturn(java.util.List.of(dto));

    ClothAttributeChangedEvent event = new ClothAttributeChangedEvent(11L, "색상");

    // when
    changedAttrListener.handler(event);

    // then
        verify(userRepository, times(1)).findActiveUserIds();
    verify(notificationService, times(1)).createAll(anySet(), contains("의상 속성 변경"), contains("색상"), eq(NotificationLevel.INFO));
    verify(sseService, times(1)).sendNotification(any());
  }
}


