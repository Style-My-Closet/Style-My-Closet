package com.stylemycloset.sse.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stylemycloset.notification.dto.NotificationDto;
import com.stylemycloset.sse.dto.NotificationDtoWithId;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.Limit;
import org.springframework.data.redis.connection.RedisStreamCommands.XAddOptions;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SseNotificationInfoCache{

  private final StringRedisTemplate template;
  private final RedisScript<Long> redisTrimScript;

  private static final String NOTIFICATION_KEY = "notification:";
  private final ObjectMapper mapper;

  @Retryable(
      retryFor = {DataAccessException.class, JsonProcessingException.class},
      maxAttempts = 2,
      backoff = @Backoff(delay = 500, multiplier = 1)
  )
  public String addNotificationInfo(Long userId, NotificationDto dto)
      throws JsonProcessingException {
    String json = mapper.writeValueAsString(dto);
    Map<String, Object> map = Map.of("payload", json);

    var options = XAddOptions.maxlen(500).approximateTrimming(true);

    RecordId recordId = template.opsForStream().add(
        StreamRecords.mapBacked(map).withStreamKey(NOTIFICATION_KEY + userId), options);
    return recordId.getValue();
  }

  public List<NotificationDtoWithId> getNotificationInfo(Long userId, String lastEventId) {
    var end = template.opsForStream()
        .reverseRange(NOTIFICATION_KEY + userId, Range.unbounded(), Limit.limit().count(1));
    if(end == null || end.isEmpty()) return List.of();

    List<NotificationDtoWithId> allNotifications = new ArrayList<>();

    String startId = lastEventId;
    String endId = end.getFirst().getId().getValue();

    while(true) {
      List<MapRecord<String, Object, Object>> data = template.opsForStream()
          .range(NOTIFICATION_KEY + userId, Range.leftOpen(startId, endId), Limit.limit().count(100));

      if(data == null || data.isEmpty()) break;

      for(var record : data) {
        String json = (String) record.getValue().get("payload");
        try{
          NotificationDto dto = mapper.readValue(json, NotificationDto.class);
          allNotifications.add(new NotificationDtoWithId(record.getId().getValue(), dto));
        } catch (JsonProcessingException e) {
          log.error("NotificationDto로 역직렬화 중 오류 발생. eventId={}, payload={}", record.getId().getValue(), json, e);
        }
      }

      startId = data.getLast().getId().getValue();
    }
    return allNotifications;
  }

  public void trimNotificationInfos() {
    long cutoffTime = System.currentTimeMillis() - Duration.ofMinutes(15).toMillis();
    String minId = cutoffTime + "-0";

    ScanOptions options = ScanOptions.scanOptions().match(NOTIFICATION_KEY + "*").count(1000).build();

    try (Cursor<String> cursor = template.scan(options)) {
      while (cursor.hasNext()) {
        String key = cursor.next();
        template.execute(
            redisTrimScript,
            List.of(key),
            minId
        );
      }
    }
  }
}
