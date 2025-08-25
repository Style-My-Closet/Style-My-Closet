package com.stylemycloset.sse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stylemycloset.IntegrationTestSupport;
import com.stylemycloset.notification.dto.NotificationDto;
import com.stylemycloset.notification.entity.NotificationLevel;
import com.stylemycloset.sse.cache.SseNotificationInfoCache;
import com.stylemycloset.sse.repository.SseRepository;
import com.stylemycloset.sse.service.impl.SseServiceImpl;
import com.stylemycloset.user.repository.UserRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@ExtendWith(OutputCaptureExtension.class)
public class SseServiceIntegrationTest extends IntegrationTestSupport {

  @Autowired
  SseServiceImpl sseService;
  @Autowired
  SseRepository sseRepository;
  @Autowired
  SseNotificationInfoCache cache;
  @Autowired
  RedisConnectionFactory connectionFactory;
  @Autowired
  StringRedisTemplate template;
  @Autowired
  UserRepository userRepository;
  @Autowired
  ObjectMapper mapper;

  String NOTIFICATION_KEY = "notification:";
  Long userId = 1L;

  @AfterEach
  void tearDown() {
    userRepository.deleteAllInBatch();
    try (var connection = connectionFactory.getConnection()) {
      connection.serverCommands().flushAll();
    }
  }

  @DisplayName("lastEventId가 없으면 새 Emitter만 만들고 반환한다.")
  @Test
  void connect_addNewEmitter_whenUnderLimit() {
    // when
    SseEmitter emitter = sseService.connect(userId, System.currentTimeMillis() + "-0", null);

    // then
    var emitters = sseRepository.findOrCreateEmitters(userId);
    assertThat(emitters).isNotEmpty();
    assertThat(emitters).contains(emitter);
  }

  @DisplayName("lastEventId와 함께 connect()를 호출할 시 알림을 조회하고 SSE로 전송한다")
  @Test
  void connect_withLastEventId_loadsMissedNotifications(CapturedOutput output) throws Exception {
    // given
    String streamKey = NOTIFICATION_KEY + userId;

    NotificationDto notificationDto = new NotificationDto(10L, Instant.now(), userId, "test", "test", NotificationLevel.INFO);
    NotificationDto notificationDto2 = new NotificationDto(11L, Instant.now(), userId, "test2", "test", NotificationLevel.INFO);
    String firstId = addToStream(streamKey, notificationDto);
    String secondId = addToStream(streamKey, notificationDto2);

    // when
    sseService.connect(userId, System.currentTimeMillis() + "-0", "1754037511000-0");

    // then
    String logs = output.getOut();
    await().untilAsserted(() -> {
      assertThat(logs).contains("놓친 알림 이벤트 개수=2");
      assertThat(logs).contains(String.format("[%d] %s SSE 이벤트 전송 성공 (eventId: %s)",
          userId, "notifications", firstId));
      assertThat(logs).contains(String.format("[%d] %s SSE 이벤트 전송 성공 (eventId: %s)",
          userId, "notifications", secondId));
    });
  }

  @DisplayName("sendNotification() 호출 시 알림 데이터를 캐시에 저장하고 SSE로 전송한다.")
  @Test
  void sendNotification_storeToCache_and_sendToEmitters(CapturedOutput output) throws Exception {
    // given
    sseService.connect(userId, System.currentTimeMillis() + "-0", null);
    NotificationDto dto = new NotificationDto(10L, Instant.now(), userId, "test", "test", NotificationLevel.INFO);

    // when
    sseService.sendNotification(dto);

    // then
    var records = template.opsForStream().reverseRange(NOTIFICATION_KEY + userId, Range.unbounded());
    assertThat(records).isNotEmpty();

    MapRecord<String, Object, Object> latest = records.getFirst();
    String json = (String) latest.getValue().get("payload");
    NotificationDto stored = mapper.readValue(json, NotificationDto.class);
    assertThat(stored).isEqualTo(dto);

    String logs = output.getOut();
    await().untilAsserted(() ->
        assertThat(logs).contains(String.format("[%d] %s SSE 이벤트 전송 성공 (eventId: %s)",
            userId, "notifications", latest.getId()))
    );
  }

  private String addToStream(String key, NotificationDto dto) throws Exception {
    Map<String, Object> body = Map.of("payload", mapper.writeValueAsString(dto));
    RecordId id = template.opsForStream().add(MapRecord.create(key, body));
    return id.getValue();
  }

  @DisplayName("cleanNotificationInfos()는 15분 이상 지난 알림은 삭제되고, 최근 알림은 유지된다")
  @Test
  void cleanNotificationInfos_removesOldOnly() throws Exception {
    // given
    String key = NOTIFICATION_KEY + userId;

    long now = System.currentTimeMillis();

    long oldMillis = now - Duration.ofMinutes(20).toMillis();
    String oldId = oldMillis + "-0";
    NotificationDto oldDto = new NotificationDto(101L, Instant.now(), userId, "test", "test", NotificationLevel.INFO);
    addToStreamWithStreamId(oldDto, oldId);

    String recentId = now + "-0";
    NotificationDto recentDto = new NotificationDto(102L, Instant.now(), userId, "newTest", "test", NotificationLevel.INFO);
    addToStreamWithStreamId(recentDto, recentId);

    // when
    cache.trimNotificationInfos();

    // then
    var records = template.opsForStream().range(key, Range.unbounded());

    assertThat(records)
        .extracting(r -> r.getId().getValue())
        .containsExactly(recentId);
  }

  private void addToStreamWithStreamId(NotificationDto dto, String streamId) throws Exception {
    template.opsForStream().add(
        MapRecord.create(NOTIFICATION_KEY + userId, Map.of("payload", mapper.writeValueAsString(dto)))
            .withId(RecordId.of(streamId))
    );
  }
}
