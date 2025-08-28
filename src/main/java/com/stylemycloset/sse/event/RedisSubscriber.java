package com.stylemycloset.sse.event;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stylemycloset.common.exception.ErrorCode;
import com.stylemycloset.common.exception.StyleMyClosetException;
import com.stylemycloset.sse.dto.NotificationTarget;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {

  private final RedisTemplate<String, Object> template;
  private final ObjectMapper mapper;
  private final RedisSubscriberService subscriberService;

  @Override
  public void onMessage(Message message, byte[] pattern) {

    NotificationTarget keys = null;
    try {
      Object raw = template.getValueSerializer().deserialize(message.getBody());
      keys = mapper.convertValue(raw, new TypeReference<>() {
      });
    } catch (Exception e) {
      log.error("알림 Subscriber에서 역직렬화 중 오류 발생", e);
      throw new StyleMyClosetException(ErrorCode.INTERNAL_SERVER_ERROR);
    }
    if (keys == null) {
      log.error("알 수 없는 이유로 알림 Subscriber의 data가 null");
      throw new StyleMyClosetException(ErrorCode.INTERNAL_SERVER_ERROR);
    }
    Set<Long> userIds = keys.userIds();

    for (Long userId : userIds) {
      subscriberService.deliver(userId);
    }
  }
}
