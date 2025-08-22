package com.stylemycloset.sse.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.stylemycloset.IntegrationTestSupport;
import com.stylemycloset.sse.dto.SseInfo;
import com.stylemycloset.sse.repository.SseRepository;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class SseControllerTest extends IntegrationTestSupport {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  SseRepository sseRepository;

  @MockitoBean
  private UserRepository userRepository;

  @MockitoBean
  UserMapper userMapper;

  @BeforeEach
  void setup() {
    User user = new User("testuser", "testuser@test.com", "testuser");
    ReflectionTestUtils.setField(user, "id", 1L);
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

  @DisplayName("lastEventId 없이 SSE 연결 요청을 보내면 SSE 연결 응답을 반환한다")
  @WithUserDetails(value = "testuser@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
  @Test
  public void sseConnect_returnSseEmitter() throws Exception {
    mockMvc.perform(get("/api/sse")
            .accept(MediaType.TEXT_EVENT_STREAM_VALUE))
        .andExpect(status().isOk())
        .andExpect(header().string("content-type",
            org.hamcrest.Matchers.containsString(MediaType.TEXT_EVENT_STREAM_VALUE)));
  }

  @DisplayName("lastEventId를 포함하여 SSE 연결 요청을 보내면 놓친 알림 데이터를 전송하고, SSE 연결 응답을 반환한다")
  @WithUserDetails(value = "testuser@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
  @Test
  public void sseReconnect_shouldReplayMissedEvents() throws Exception {
    // given
    long userId = 1L;

    var sseInfos = List.of(
        new SseInfo(1854037511000L, "notificaiton", "데이터A", System.currentTimeMillis()),
        new SseInfo(1954037511000L, "notificaiton", "데이터B", System.currentTimeMillis())
    );
    sseRepository.findOrCreateEvents(userId).addAll(sseInfos);

    // when & then
    mockMvc.perform(get("/api/sse")
            .param("lastEventId", "1754037511000")
            .accept(MediaType.TEXT_EVENT_STREAM_VALUE))
        .andExpect(status().isOk())
        .andExpect(content().string(org.hamcrest.Matchers.containsString("데이터A")))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("데이터B")))
        .andExpect(header().string("content-type",
            org.hamcrest.Matchers.containsString(MediaType.TEXT_EVENT_STREAM_VALUE)));
  }

  @DisplayName("잘못된 형식의 lastEventId를 포함하여 SSE 연결 요청을 보내면 경고 로그를 보낸다")
  @WithUserDetails(value = "testuser@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
  @Test
  public void sseReconnect_withInvalidLastEventId_shouldWriteWarnLog() throws Exception {
    mockMvc.perform(get("/api/sse")
            .param("lastEventId", "1754037511000L")
            .accept(MediaType.TEXT_EVENT_STREAM_VALUE))
        .andExpect(status().isOk());
  }
}
