package com.stylemycloset.notification.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.stylemycloset.notification.entity.Notification;
import com.stylemycloset.notification.entity.NotificationLevel;
import com.stylemycloset.notification.event.listener.NotificationEventListener;
import com.stylemycloset.notification.repository.NotificationRepository;
import com.stylemycloset.sse.repository.SseRepository;
import com.stylemycloset.sse.service.SseService;
import com.stylemycloset.testutil.IntegrationTestSupport;
import com.stylemycloset.user.dto.request.UserCreateRequest;
import com.stylemycloset.user.entity.Role;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.user.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public class NotificationEventListenerIntegrationTest extends IntegrationTestSupport {

  @Autowired
  NotificationEventListener notificationEventListener;

  @MockitoBean
  NotificationRepository notificationRepository;

  @MockitoBean
  UserRepository userRepository;

  @Autowired
  SseService sseService;

  @Mock
  SseRepository sseRepository;

  User createUser(String name, String email, Long id) {
    UserCreateRequest request = new UserCreateRequest(name, email, "test");
    User user = new User(request);
    ReflectionTestUtils.setField(user, "id", id);
    return user;
  }

  @DisplayName("사용자 권한 변경 이벤트가 호출되면 알림을 생성하고 SSE로 전송 후 로그를 띄운다")
  @Test
  void handleRoleChangedEvent_sendSseMessage() throws Exception {
    // given
    User user = createUser("test", "test@test.test", 1L);
    ReflectionTestUtils.setField(user, "role", Role.USER);

    String now = String.valueOf(System.currentTimeMillis());
    SseEmitter emitter = sseService.connect(user.getId(), now, null);

    given(notificationRepository.save(any(Notification.class)))
        .willAnswer(invocation -> {
          Notification n = invocation.getArgument(0);
          ReflectionTestUtils.setField(n, "id", 1L);
          ReflectionTestUtils.setField(n, "createdAt", Instant.now());
          return n;
        });
    given(sseRepository.findByUserId(user.getId())).willReturn(List.of(emitter));

    RoleChangedEvent roleChangedEvent = new RoleChangedEvent(user, Role.ADMIN);

    // when
    notificationEventListener.handleRoleChangedEvent(roleChangedEvent);

    // then
    await().untilAsserted(() -> {
      ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
      verify(notificationRepository).save(captor.capture());

      Notification saved = captor.getValue();
      assertThat(saved.getReceiver()).isEqualTo(user);
      assertThat(saved.getTitle()).isEqualTo("내 권한이 변경되었어요.");
      assertThat(saved.getLevel()).isEqualTo(NotificationLevel.INFO);
    });
  }

  @DisplayName("의상 속성 추가 이벤트가 호출되면 알림을 생성하고 SSE로 전송 후 로그를 띄운다")
  @Test
  void handleNewClothAttributeEvent_sendSseMessage() throws Exception {
    // given
    User user = createUser("test", "test@test.test", 1L);
    User user2 = createUser("test2", "test2@test.test", 2L);
    Set<User> users = Set.of(user, user2);

    String now = String.valueOf(System.currentTimeMillis());
    SseEmitter emitter = sseService.connect(user.getId(), now, null);
    SseEmitter emitter2 = sseService.connect(user2.getId(), now, null);

    AtomicLong idGenerator = new AtomicLong();

    given(userRepository.findByLockedFalseAndDeleteAtIsNull()).willReturn(users);
    given(notificationRepository.saveAll(anyList()))
        .willAnswer(invocation -> {
          List<Notification> notifications = invocation.getArgument(0);
          Instant createdAt = Instant.now();

          for(Notification notification : notifications) {
            ReflectionTestUtils.setField(notification, "id", idGenerator.getAndIncrement());
            ReflectionTestUtils.setField(notification, "createdAt", createdAt);
          }
          return notifications;
        });
    given(sseRepository.findByUserId(user.getId())).willReturn(List.of(emitter));
    given(sseRepository.findByUserId(user2.getId())).willReturn(List.of(emitter2));

    NewClothAttributeEvent event = new NewClothAttributeEvent(1L, "속성 추가");

    //when
    notificationEventListener.handleNewClothAttributeEvent(event);

    // then
    await().untilAsserted(() -> verify(notificationRepository).saveAll(any(List.class)));
  }

  @DisplayName("의상 속성 변경 이벤트가 호출되면 알림을 생성하고 SSE로 전송 후 로그를 띄운다")
  @Test
  void handleClothAttributeChangedEvent_sendSseMessage() throws Exception {
    // given
    User user = createUser("test", "test@test.test", 1L);
    User user2 = createUser("test2", "test2@test.test", 2L);
    Set<User> users = Set.of(user, user2);

    String now = String.valueOf(System.currentTimeMillis());
    SseEmitter emitter = sseService.connect(user.getId(), now, null);
    SseEmitter emitter2 = sseService.connect(user2.getId(), now, null);

    AtomicLong idGenerator = new AtomicLong();

    given(userRepository.findByLockedFalseAndDeleteAtIsNull()).willReturn(users);
    given(notificationRepository.saveAll(anyList()))
        .willAnswer(invocation -> {
          List<Notification> notifications = invocation.getArgument(0);
          Instant createdAt = Instant.now();

          for(Notification notification : notifications) {
            ReflectionTestUtils.setField(notification, "id", idGenerator.getAndIncrement());
            ReflectionTestUtils.setField(notification, "createdAt", createdAt);
          }
          return notifications;
        });
    given(sseRepository.findByUserId(user.getId())).willReturn(List.of(emitter));
    given(sseRepository.findByUserId(user2.getId())).willReturn(List.of(emitter2));

    ClothAttributeChangedEvent event = new ClothAttributeChangedEvent(1L, "속성 변경");

    //when
    notificationEventListener.handleClothAttributeChangedEvent(event);

    // then
    await().untilAsserted(() -> verify(notificationRepository).saveAll(any(List.class)));
  }

  @DisplayName("피드 좋아요 이벤트가 호출되면 알림을 생성하고 SSE로 전송 후 로그를 띄운다")
  @Test
  void handleFeedLikeEvent_sendSseMessage() throws Exception {
    // given
    User user = createUser("test", "test@test.test", 1L);
    ReflectionTestUtils.setField(user, "role", Role.USER);

    String now = String.valueOf(System.currentTimeMillis());
    SseEmitter emitter = sseService.connect(user.getId(), now, null);

    given(notificationRepository.save(any(Notification.class)))
        .willAnswer(invocation -> {
          Notification n = invocation.getArgument(0);
          ReflectionTestUtils.setField(n, "id", 1L);
          ReflectionTestUtils.setField(n, "createdAt", Instant.now());
          return n;
        });
    given(sseRepository.findByUserId(user.getId())).willReturn(List.of(emitter));

    FeedLikedEvent event = new FeedLikedEvent(1L, "피드 좋아요 테스트", user, "user2");

    // when
    notificationEventListener.handleFeedLikeEvent(event);

    // then
    await().untilAsserted(() -> {
      ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
      verify(notificationRepository).save(captor.capture());

      Notification saved = captor.getValue();
      assertThat(saved.getReceiver()).isEqualTo(user);
      assertThat(saved.getTitle()).isEqualTo("user2님이 내 피드를 좋아합니다.");
      assertThat(saved.getLevel()).isEqualTo(NotificationLevel.INFO);
    });
  }

  @DisplayName("피드 댓글 이벤트가 호출되면 알림을 생성하고 SSE로 전송 후 로그를 띄운다")
  @Test
  void handleCommentEvent_sendSseMessage() throws Exception {
    // given
    User user = createUser("test", "test@test.test", 1L);
    ReflectionTestUtils.setField(user, "role", Role.USER);

    String now = String.valueOf(System.currentTimeMillis());
    SseEmitter emitter = sseService.connect(user.getId(), now, null);

    given(notificationRepository.save(any(Notification.class)))
        .willAnswer(invocation -> {
          Notification n = invocation.getArgument(0);
          ReflectionTestUtils.setField(n, "id", 1L);
          ReflectionTestUtils.setField(n, "createdAt", Instant.now());
          return n;
        });
    given(sseRepository.findByUserId(user.getId())).willReturn(List.of(emitter));

    FeedCommentEvent event = new FeedCommentEvent(1L, "user2", "피드 좋아요 테스트", user);

    // when
    notificationEventListener.handleCommentEvent(event);

    // then
    await().untilAsserted(() -> {
      ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
      verify(notificationRepository).save(captor.capture());

      Notification saved = captor.getValue();
      assertThat(saved.getReceiver()).isEqualTo(user);
      assertThat(saved.getTitle()).isEqualTo("user2님이 댓글을 달았어요.");
      assertThat(saved.getLevel()).isEqualTo(NotificationLevel.INFO);
    });
  }

}
