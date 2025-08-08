package com.stylemycloset.sse;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.stylemycloset.sse.dto.SseInfo;
import com.stylemycloset.sse.service.impl.SseServiceImpl;
import com.stylemycloset.testutil.IntegrationTestSupport;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class SseControllerTest extends IntegrationTestSupport {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  SseServiceImpl sseServiceImpl;

  @DisplayName("lastEventId 없이 SSE 연결 요청을 보내면 SSE 연결 응답을 반환한다")
  @WithMockUser(username = "testuser", roles = "USER")
  @Test
  public void sseConnect_returnSseEmitter() throws Exception {
    // given
    Long userId = 1L;

    // when & then
    mockMvc.perform(get("/api/sse/{userId}", userId)
            .accept(MediaType.TEXT_EVENT_STREAM_VALUE))
        .andExpect(status().isOk())
        .andExpect(header().string("content-type", org.hamcrest.Matchers.containsString(MediaType.TEXT_EVENT_STREAM_VALUE)));
  }

  @DisplayName("lastEventId를 포함하여 SSE 연결 요청을 보내면 놓친 알림 데이터를 전송하고, SSE 연결 응답을 반환한다")
  @WithMockUser(username = "testuser", roles = "USER")
  @Test
  public void sseReconnect_shouldReplayMissedEvents() throws Exception {
    // given
    long userId = 1L;

    List<SseInfo> sseInfos = List.of(
        new SseInfo(1854037511000L, "notificaiton", "데이터A", System.currentTimeMillis()),
        new SseInfo(1954037511000L, "notificaiton", "데이터B", System.currentTimeMillis())
    );

    Field field = SseServiceImpl.class.getDeclaredField("userEvents");
    field.setAccessible(true);
    Map<Long, List<SseInfo>> userEvents = (Map<Long, List<SseInfo>>) field.get(sseServiceImpl);
    userEvents.put(userId, new CopyOnWriteArrayList<>(sseInfos));

    // when & then
    mockMvc.perform(get("/api/sse/{userId}", userId)
            .param("lastEventId", "1754037511000")
            .accept(MediaType.TEXT_EVENT_STREAM_VALUE))
        .andExpect(status().isOk())
        .andExpect(content().string(org.hamcrest.Matchers.containsString("데이터A")))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("데이터B")))
        .andExpect(header().string("content-type", org.hamcrest.Matchers.containsString(MediaType.TEXT_EVENT_STREAM_VALUE)));
  }

  @DisplayName("잘못된 형식의 lastEventId를 포함하여 SSE 연결 요청을 보내면 경고 로그를 보낸다")
  @WithMockUser(username = "testuser", roles = "USER")
  @Test
  public void sseReconnect_withInvalidLastEventId_shouldWriteWarnLog() throws Exception {
    long userId = 1L;

    mockMvc.perform(get("/api/sse/{userId}", userId)
            .param("lastEventId", "1754037511000L")
            .accept(MediaType.TEXT_EVENT_STREAM_VALUE))
        .andExpect(status().isOk());
  }
}
