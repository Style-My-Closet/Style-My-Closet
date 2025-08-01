package com.stylemycloset.sse;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.stylemycloset.testutil.IntegrationTestSupport;
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


  @DisplayName("SSE 연결 요청을 보내면 SSE 연결 응답을 반환한다")
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
}
