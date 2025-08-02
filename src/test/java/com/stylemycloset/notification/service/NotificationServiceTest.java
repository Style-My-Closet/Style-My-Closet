package com.stylemycloset.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.stylemycloset.notification.dto.NotificationDto;
import com.stylemycloset.notification.dto.NotificationDtoCursorResponse;
import com.stylemycloset.notification.dto.NotificationFindAllRequest;
import com.stylemycloset.notification.entity.Notification;
import com.stylemycloset.notification.entity.NotificationLevel;
import com.stylemycloset.notification.repository.NotificationQueryRepository;
import com.stylemycloset.notification.repository.NotificationRepository;
import com.stylemycloset.notification.service.impl.NotificationServiceImpl;
import com.stylemycloset.testutil.IntegrationTestSupport;
import com.stylemycloset.user.dto.UserCreateRequest;
import com.stylemycloset.user.entity.User;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.util.ReflectionTestUtils;

public class NotificationServiceTest extends IntegrationTestSupport {

  @Mock
  NotificationRepository notificationRepository;

  @Mock
  NotificationQueryRepository notificationQueryRepository;

  @InjectMocks
  NotificationServiceImpl notificationService;

  private User user1;
  private User user2;
  private User user3;

  @BeforeEach
  void setup() {
    UserCreateRequest request1 = new UserCreateRequest("test", "test@test.test", "test");
    UserCreateRequest request2 = new UserCreateRequest("test2", "test2@test.test", "test");
    UserCreateRequest request3 = new UserCreateRequest("test3", "test3@test.test", "test");

    user1 = new User(request1);
    user2 = new User(request2);
    user3 = new User(request3);

    ReflectionTestUtils.setField(user1, "id", 1L);
    ReflectionTestUtils.setField(user2, "id", 2L);
    ReflectionTestUtils.setField(user3, "id", 3L);
  }

  @DisplayName("알림 생성 시 NotificationDto로 반환된다")
  @WithMockUser(username = "testuser", roles = "USER")
  @Test
  void createNotification_shouldReturnNotificationDto() throws Exception {
    // when
    NotificationDto result =
        notificationService.create(user1, "testTitle", "testContent", NotificationLevel.INFO);

    // then 
    assertThat(result.getClass()).isEqualTo(NotificationDto.class);
    assertThat(result.title()).isEqualTo("testTitle");
    assertThat(result.content()).isEqualTo("testContent");
    assertThat(result.level()).isEqualTo(NotificationLevel.INFO);
  }

  @DisplayName("여러 알림 생성 시 List<NotificationDto>로 반환된다")
  @WithMockUser(username = "testuser", roles = "USER")
  @Test
  void createAllNotification_shouldReturnNotificationDtos() throws Exception {
    // given
    Set<User> users = Set.of(user1, user2, user3);

    // when
    List<NotificationDto> result =
        notificationService.createAll(users, "testTitle", "testContent", NotificationLevel.INFO);

    // then
    assertThat(result.size()).isEqualTo(3);
    assertThat(result.get(0).title()).isEqualTo("testTitle");
    assertThat(result.get(1).level()).isEqualTo(NotificationLevel.INFO);
  }

  @DisplayName("알림 삭제를 요청하면 알림이 삭제된다")
  @WithMockUser(username = "testuser", roles = "USER")
  @Test
  void deleteNotification_shouldReturnVoid() throws Exception {
    // given
    Notification notification = new Notification(user1, "testTitle", "testContent", NotificationLevel.INFO);
    ReflectionTestUtils.setField(notification, "id", 1L);
    given(notificationRepository.findById(1L)).willReturn(Optional.of(notification));

    // when
    notificationService.delete(1,1);

    // then
    verify(notificationRepository).delete(notification);
  }

  @DisplayName("존재하지 않는 알림을 삭제하면 로그를 찍고 void를 반환한다")
  @WithMockUser(username = "testuser", roles = "USER")
  @Test
  void deleteNotification_whenNotificationIsNull_shouldLogInfo() throws Exception {
    // when
    notificationService.delete(1,1);

    // then
    verify(notificationRepository, never()).delete(any(Notification.class));
  }

  @DisplayName("알림 목록 조회를 요청하면 NotificationDtoCursorResponse이 반환된다")
  @WithMockUser(username = "testuser", roles = "USER")
  @Test
  void findAllNotification_shouldReturnNotificationDtoCursorResponse() throws Exception {
    // given
    Notification n1 =  new Notification(user1, "testTitle", "testContent", NotificationLevel.INFO);
    Notification n2 =  new Notification(user1, "testTitle", "testContent", NotificationLevel.INFO);
    Notification n3 =  new Notification(user1, "testTitle", "testContent", NotificationLevel.WARNING);

    Instant createdAt1 = Instant.parse("2025-07-01T10:15:30.00Z");
    Instant createdAt2 = Instant.parse("2025-07-02T10:15:30.00Z");
    Instant createdAt3 = Instant.parse("2025-07-03T10:15:30.00Z");

    ReflectionTestUtils.setField(n1, "id", 1L);
    ReflectionTestUtils.setField(n2, "id", 2L);
    ReflectionTestUtils.setField(n3, "id", 3L);

    ReflectionTestUtils.setField(n1, "createdAt", createdAt1);
    ReflectionTestUtils.setField(n2, "createdAt", createdAt2);
    ReflectionTestUtils.setField(n3, "createdAt", createdAt3);

    List<Notification> notifications = List.of(n3, n2, n1);
    NotificationFindAllRequest request = new NotificationFindAllRequest(null, null, 2);

    given(notificationQueryRepository.findAllByCursor(request, 1)).willReturn(notifications);
    given(notificationRepository.countByReceiverId(1)).willReturn(3L);

    // when
    NotificationDtoCursorResponse result = notificationService.findAll(1,  request);

    // then
    assertThat(result.data().size()).isEqualTo(2);
    assertThat(result.nextCursor()).isEqualTo(Instant.parse("2025-07-02T10:15:30.00Z"));
    assertThat(result.nextIdAfter()).isEqualTo(2);
    assertThat(result.hasNext()).isTrue();

  }

}
