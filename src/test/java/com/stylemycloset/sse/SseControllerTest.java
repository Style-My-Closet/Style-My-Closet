package com.stylemycloset.sse;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class SseControllerTest {

  @Autowired
  MockMvc mockMvc;


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
