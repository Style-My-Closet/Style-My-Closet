package com.stylemycloset.notification.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.stylemycloset.IntegrationTestSupport;
import com.stylemycloset.notification.entity.Notification;
import com.stylemycloset.notification.entity.NotificationLevel;
import com.stylemycloset.notification.repository.NotificationRepository;
import com.stylemycloset.user.dto.data.UserDto;
import com.stylemycloset.user.entity.Role;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.user.mapper.UserMapper;
import com.stylemycloset.user.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc(addFilters = false)
public class NotificationControllerTest extends IntegrationTestSupport {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  NotificationRepository notificationRepository;

  @MockitoBean
  private UserRepository userRepository;

  @MockitoBean
  UserMapper userMapper;

  Long userId = 1L;

  @BeforeEach
  void setup() {
    notificationRepository.deleteAllInBatch();
    User user = new User("testuser", "testuser@test.com", "testuser");
    ReflectionTestUtils.setField(user, "id", userId);
    UserDto dto = new UserDto(
        user.getId(),
        Instant.parse("2025-08-13T07:00:00Z"),
        user.getEmail(),
        user.getName(),
        Role.USER,
        List.of(),
        false
    );

    given(userRepository.findByEmail("testuser@test.com"))
        .willReturn(Optional.of(user));
    given(userMapper.toUserDto(user)).willReturn(dto);
  }

  @DisplayName("알림 삭제 요청을 보낼 시 204응답이 온다")
  @WithUserDetails(value = "testuser@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
  @Test
  void deleteNotifications_ReturnIsNoContent() throws Exception {
    // given
    Notification notification = notificationRepository.save(
        new Notification(userId, "testTitle", "testContent", NotificationLevel.INFO));

    // when & then
    mockMvc.perform(
            delete("/api/notifications/{notificationId}", notification.getId()))
        .andExpect(status().isNoContent());
  }

  @DisplayName("알림 조회 요청을 보낼 시 NotificationDtoCursorResponse가 응답으로 온다")
  @WithUserDetails(value = "testuser@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
  @Test
  void findAllByReceiverId_shouldReturnNotificationDtoCursorResponse() throws Exception {
    // given
    notificationRepository.save(
        new Notification(userId, "testTitle", "testContent", NotificationLevel.INFO));
    notificationRepository.save(
        new Notification(userId, "testTitle", "testContent", NotificationLevel.INFO));
    notificationRepository.save(
        new Notification(userId, "testTitle", "testContent", NotificationLevel.INFO));

    notificationRepository.flush();

    // when & then
    mockMvc.perform(
            get("/api/notifications")
                .param("limit", String.valueOf(2))
        ).andExpect(status().isOk())
        .andExpect(jsonPath("$.hasNext").value(true))
        .andExpect(jsonPath("$.totalCount").value(3));
  }
}
