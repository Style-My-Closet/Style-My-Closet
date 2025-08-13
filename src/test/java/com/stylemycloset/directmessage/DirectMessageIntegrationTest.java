package com.stylemycloset.directmessage;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.stylemycloset.IntegrationTestSupport;
import com.stylemycloset.directmessage.dto.DirectMessageResult;
import com.stylemycloset.directmessage.dto.request.DirectMessageCreateRequest;
import com.stylemycloset.directmessage.entity.DirectMessageKey;
import com.stylemycloset.directmessage.repository.DirectMessageRepository;
import com.stylemycloset.notification.dto.NotificationDto;
import com.stylemycloset.sse.service.SseService;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.user.repository.UserRepository;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeoutException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DirectMessageIntegrationTest extends IntegrationTestSupport {

  @Autowired
  private UserRepository userRepository;
  @Autowired
  private DirectMessageRepository messageRepository;

  @MockitoBean
  private SseService sseService;

  @LocalServerPort
  private int port;

  private StompSession senderSession;
  private StompSession receiverSession;

  @AfterEach
  void tearDown() {
    // 연결 종료
    if (senderSession != null && senderSession.isConnected()) {
      senderSession.disconnect();
    }
    if (receiverSession != null && receiverSession.isConnected()) {
      receiverSession.disconnect();
    }
    userRepository.deleteAllInBatch();
    messageRepository.deleteAllInBatch();
  }

  @DisplayName("DM 메세지를 상대방에게 전송하면, 상대방은 메세지를 받습니다.")
  @Test
  void send_and_receive() throws Exception {
    // given
    User sender = userRepository.save(new User("alice", "a@a.com", "pwd"));
    User receiver = userRepository.save(new User("bob", "b@b.com", "pwd"));

    senderSession = createClientSession();
    receiverSession = createClientSession();
    BlockingQueue<DirectMessageResult> messageMailbox = createMessageMailbox(receiverSession,
        sender, receiver);

    // when
    String content = UUID.randomUUID().toString();
    DirectMessageCreateRequest request =
        new DirectMessageCreateRequest(sender.getId(), receiver.getId(), content);
    senderSession.send("/pub/direct-messages_send", request);

    if (TestTransaction.isActive()) {
      TestTransaction.flagForCommit();
      TestTransaction.end();
    }

    // then
    await()
        .atMost(Duration.ofSeconds(10))
        .until(() -> !messageMailbox.isEmpty());

    DirectMessageResult received = messageMailbox.poll();
    Assertions.assertThat(received)
        .extracting(
            result -> result.sender().id(),
            result -> result.receiver().id(),
            DirectMessageResult::content
        )
        .containsExactly(sender.getId(), receiver.getId(), content);
    verify(sseService).sendNotification(any(NotificationDto.class));
  }

  private StompSession createClientSession()
      throws InterruptedException, ExecutionException, TimeoutException {
    List<Transport> transports = List.of(new WebSocketTransport(new StandardWebSocketClient()));
    SockJsClient sockJsClient = new SockJsClient(transports);
    WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);

    // ★ Jackson 설정: JavaTimeModule 등록
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
    converter.setObjectMapper(mapper);
    stompClient.setMessageConverter(converter);

    String url = "ws://localhost:" + port + "/ws";
    return stompClient.connectAsync(url, new StompSessionHandlerAdapter() {
    }).get(3, TimeUnit.SECONDS);
  }

  private BlockingQueue<DirectMessageResult> createMessageMailbox(
      StompSession session,
      User sender,
      User receiver
  ) {
    String destination = "/sub/direct-messages_" + DirectMessageKey.of(sender, receiver);
    BlockingQueue<DirectMessageResult> inbox = new LinkedBlockingQueue<>();
    session.subscribe(destination, new StompFrameHandler() {
      @Override
      public Type getPayloadType(StompHeaders headers) {
        return DirectMessageResult.class;
      }

      @Override
      public void handleFrame(StompHeaders headers, Object payload) {
        inbox.offer((DirectMessageResult) payload);
      }
    });

    return inbox;
  }

}