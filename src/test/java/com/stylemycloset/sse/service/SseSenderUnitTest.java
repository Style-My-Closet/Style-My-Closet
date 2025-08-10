package com.stylemycloset.sse.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.awaitility.Awaitility.await;

import com.stylemycloset.sse.repository.SseRepository;
import com.stylemycloset.testutil.IntegrationTestSupport;
import java.io.IOException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public class SseSenderUnitTest extends IntegrationTestSupport {
  @Autowired
  SseSender sseSender;

  @MockitoBean
  SseRepository sseRepository;

  @Mock
  SseEmitter emitter;

  @DisplayName("sendToClient가 호출되면 send()를 호출한다")
  @Test
  void sendToClientAsync_success() throws Exception {
    // when
    sseSender.sendToClientAsync(
        1L,
        emitter,
        String.valueOf(System.currentTimeMillis()),
        "notifications",
        "테스트"
    );

    // then
    await().untilAsserted(() -> verify(emitter).send(any(SseEmitter.SseEventBuilder.class)));
  }

  @DisplayName("sendToClient가 IOException으로 실패하면 재시도 후 delete()를 호출한다")
  @Test
  void sendToClientAsync_ioException() throws Exception {
    // given
    doThrow(new IOException("SSE 전송 실패")).when(emitter).send(any(SseEmitter.SseEventBuilder.class));

    // when
    sseSender.sendToClientAsync(
        1L,
        emitter,
        String.valueOf(System.currentTimeMillis()),
        "notifications",
        "테스트"
    );

    // then
    await().untilAsserted(() -> verify(sseRepository).removeEmitter(1L, emitter));
  }
}
