package com.stylemycloset.notification.event;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.stylemycloset.notification.dto.NotificationDto;
import com.stylemycloset.notification.entity.NotificationLevel;
import com.stylemycloset.notification.event.listener.NotificationEventListener;
import com.stylemycloset.notification.service.NotificationService;
import com.stylemycloset.sse.service.SseService;
import com.stylemycloset.testutil.IntegrationTestSupport;
import com.stylemycloset.user.dto.request.UserCreateRequest;
import com.stylemycloset.user.entity.Role;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.user.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;

public class NotificationEventListenerUnitTest extends IntegrationTestSupport {

  @Autowired
  NotificationEventListener notificationEventListener;

  @MockitoBean
  NotificationService notificationService;

  @MockitoBean
  SseService sseService;

  @MockitoBean
  UserRepository userRepository;

  User createUser(String name, String email, Long id) {
    UserCreateRequest request = new UserCreateRequest(name, email, "test");
    User user = new User(request);
    ReflectionTestUtils.setField(user, "id", id);
    return user;
  }

  NotificationDto createNotificationDto(Long id, Long receiverId) {
    return NotificationDto.builder()
        .id(id)
        .receiverId(receiverId)
        .title("title")
        .content("content")
        .level(NotificationLevel.INFO)
        .createdAt(Instant.now())
        .build();
  }

  @DisplayName("사용자 권한 변경 이벤트가 호출되면 알림을 생성하고 sendNotification을 호출한다.")
  @Test
  void handleRoleChangedEvent_sendSseMessage() throws Exception {
    // given
    User user = createUser("name", "test@test.email", 1L);
    ReflectionTestUtils.setField(user, "role", Role.USER);
    NotificationDto fakeNotificationDto = createNotificationDto(1L, 1L);

    given(notificationService.create(
        any(User.class), any(String.class), any(String.class), any(NotificationLevel.class)))
        .willReturn(fakeNotificationDto);

    RoleChangedEvent roleChangedEvent = new RoleChangedEvent(user, Role.ADMIN);

    // when
    notificationEventListener.handleRoleChangedEvent(roleChangedEvent);

    // then
    String expectedTitle = "내 권한이 변경되었어요.";
    String expectedContent = "내 권한이 [USER]에서 [ADMIN]로 변경되었어요.";

    await().untilAsserted(() -> {
      verify(notificationService).create(
          eq(user), eq(expectedTitle), eq(expectedContent), eq(NotificationLevel.INFO));
      verify(sseService).sendNotification(eq(fakeNotificationDto));
    });
  }

  @DisplayName("의상 속성 추가 이벤트가 호출되면 알림을 생성하고 sendNotification을 호출한다.")
  @Test
  void handleNewClothAttributesEvent_sendSseMessage() throws Exception {
    // given
    User user = createUser("name", "test@test.email", 1L);
    User user2 = createUser("name2", "test2@test.email", 2L);
    Set<User> users = Set.of(user, user2);

    NotificationDto fakeNotificationDto1 = createNotificationDto(1L, 1L);
    NotificationDto fakeNotificationDto2 = createNotificationDto(2L, 2L);
    List<NotificationDto> notificationDtoList = List.of(fakeNotificationDto1, fakeNotificationDto2);

    given(userRepository.findByLockedFalseAndDeleteAtIsNull()).willReturn(users);
    given(notificationService.createAll(
        any(Set.class), any(String.class), any(String.class), any(NotificationLevel.class)))
        .willReturn(notificationDtoList);

    NewClothAttributeEvent event = new NewClothAttributeEvent(2L, "테스트 속성");

    // when
    notificationEventListener.handleNewClothAttributeEvent(event);

    //then
    String expectedTitle = "새로운 의상 속성이 추가되었어요.";
    String expectedContent = "내 의상에 [테스트 속성] 속성을 추가해보세요.";

    await().untilAsserted(() -> {
      verify(notificationService).createAll(
          eq(users), eq(expectedTitle), eq(expectedContent), eq(NotificationLevel.INFO));
      verify(sseService).sendNotification(eq(fakeNotificationDto1));
      verify(sseService).sendNotification(eq(fakeNotificationDto2));
    });
  }

  @DisplayName("의상 속성 변경 이벤트가 호출되면 알림을 생성하고 sendNotification을 호출한다.")
  @Test
  void handleClothAttributeChangedEvent_sendSseMessage() throws Exception {
    // given
    User user = createUser("name", "test@test.email", 1L);
    User user2 = createUser("name2", "test2@test.email", 2L);
    Set<User> users = Set.of(user, user2);

    NotificationDto fakeNotificationDto1 = createNotificationDto(1L, 1L);
    NotificationDto fakeNotificationDto2 = createNotificationDto(2L, 2L);
    List<NotificationDto> notificationDtoList = List.of(fakeNotificationDto1, fakeNotificationDto2);

    given(userRepository.findByLockedFalseAndDeleteAtIsNull()).willReturn(users);
    given(notificationService.createAll(
        any(Set.class), any(String.class), any(String.class), any(NotificationLevel.class)))
        .willReturn(notificationDtoList);

    ClothAttributeChangedEvent event = new ClothAttributeChangedEvent(1L, "변경된 속성");

    // when
    notificationEventListener.handleClothAttributeChangedEvent(event);

    // then
    String expectedTitle = "의상 속성이 변경되었어요.";
    String expectedContent = "[변경된 속성] 속성을 확인해보세요.";
    await().untilAsserted(() -> {
      verify(notificationService).createAll(
          eq(users), eq(expectedTitle), eq(expectedContent), eq(NotificationLevel.INFO));
      verify(sseService).sendNotification(eq(fakeNotificationDto1));
      verify(sseService).sendNotification(eq(fakeNotificationDto2));
    });
  }

  @DisplayName("피드 좋아요 이벤트가 호출되면 알림을 생성하고 sendNotification을 호출한다.")
  @Test
  void handleFeedLikeEvent_sendSseMessage() throws Exception {
    // given
    User user = createUser("name", "test@test.email", 1L);
    NotificationDto fakeNotificationDto = createNotificationDto(1L, 1L);

    given(notificationService.create(
        any(User.class), any(String.class), any(String.class), any(NotificationLevel.class)))
        .willReturn(fakeNotificationDto);

    FeedLikedEvent event = new FeedLikedEvent(1L, "테스트 피드", user, "사용자2");

    // when
    notificationEventListener.handleFeedLikeEvent(event);

    // then
    String expectedTitle = "사용자2님이 내 피드를 좋아합니다.";
    String expectedContent = "테스트 피드";
    await().untilAsserted(() -> {
      verify(notificationService).create(
          eq(user), eq(expectedTitle), eq(expectedContent), eq(NotificationLevel.INFO));
      verify(sseService).sendNotification(eq(fakeNotificationDto));
    });
  }

  @DisplayName("피드 댓글 이벤트가 호출되면 알림을 생성하고 sendNotification을 호출한다.")
  @Test
  void handleCommentEvent_sendSseMessage() throws Exception {
    // given
    User user = createUser("name", "test@test.email", 1L);
    NotificationDto fakeNotificationDto = createNotificationDto(1L, 1L);

    given(notificationService.create(
        any(User.class), any(String.class), any(String.class), any(NotificationLevel.class)))
        .willReturn(fakeNotificationDto);

    FeedCommentEvent event =
        new FeedCommentEvent(1L, "사용자2", "테스트 댓글", user);

    // when
    notificationEventListener.handleCommentEvent(event);

    // then
    String expectedTitle = "사용자2님이 댓글을 달았어요.";
    String expectedContent = "테스트 댓글";
    await().untilAsserted(() -> {
      verify(notificationService).create(
          eq(user), eq(expectedTitle), eq(expectedContent), eq(NotificationLevel.INFO));
      verify(sseService).sendNotification(eq(fakeNotificationDto));
    });
  }
}