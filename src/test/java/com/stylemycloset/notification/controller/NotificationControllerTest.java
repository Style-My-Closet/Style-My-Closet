package com.stylemycloset.notification.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.stylemycloset.notification.entity.Notification;
import com.stylemycloset.notification.entity.NotificationLevel;
import com.stylemycloset.notification.repository.NotificationRepository;
import com.stylemycloset.testutil.IntegrationTestSupport;
import com.stylemycloset.user.dto.UserCreateRequest;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
public class NotificationControllerTest extends IntegrationTestSupport {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  NotificationRepository notificationRepository;

  @Autowired
  UserRepository userRepository;

  @DisplayName("알림 삭제 요청을 보낼 시 204응답이 온다")
  @Test
  void deleteNotifications_ReturnIsNoContent() throws Exception{
    // given
    UserCreateRequest request1 = new UserCreateRequest("test", "test@test.test", "test");
    User user1 = userRepository.save(new User(request1));

    Notification notification = notificationRepository.save(
        new Notification(user1, "testTitle", "testContent", NotificationLevel.INFO));

    // when & then
    mockMvc.perform(
        delete("/api/notifications/{receiverId}/{notificationId}",
            user1.getId(), notification.getId()))
        .andExpect(status().isNoContent());
  }

  @DisplayName("알림 조회 요청을 보낼 시 NotificationDtoCursorResponse가 응답으로 온다")
  @Test
  void findAllByReceiverId_shouldReturnNotificationDtoCursorResponse() throws Exception{
    // given
    UserCreateRequest request4 = new UserCreateRequest("test", "test@test.test", "test");
    User user4 = new User(request4);
    userRepository.save(user4);

    notificationRepository.save(
        new Notification(user4, "testTitle", "testContent", NotificationLevel.INFO));
    notificationRepository.save(
        new Notification(user4, "testTitle", "testContent", NotificationLevel.INFO));
    notificationRepository.save(
        new Notification(user4, "testTitle", "testContent", NotificationLevel.INFO));

    notificationRepository.flush();

    // when & then
    mockMvc.perform(
        get("/api/notifications/{userId}", user4.getId())
            .param("limit", String.valueOf(2))
    ).andExpect(status().isOk())
        .andExpect(jsonPath("$.hasNext").value(true))
        .andExpect(jsonPath("$.totalCount").value(3));
  }
}
