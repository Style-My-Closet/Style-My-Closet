package com.stylemycloset.notification.event;

import com.stylemycloset.notification.dto.NotificationDto;
import com.stylemycloset.sse.cache.SseNotificationInfoCache;
import com.stylemycloset.sse.dto.NotificationTarget;
import com.stylemycloset.sse.event.RedisPublishListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationStreamPublisher {
  private final SseNotificationInfoCache cache;
  private final RedisPublishListener redisPublishListener;

  public void processAndPublish(List<NotificationDto> dtoList) {
    Set<Long> userIds = new HashSet<>();
    for(NotificationDto dto : dtoList){
      try{
        cache.addNotificationInfo(dto.receiverId(), dto);
        userIds.add(dto.receiverId());
      } catch (Exception e){
        log.error("캐시 저장 중 오류 발생, 알림 전송 스킵 : userId={}, notificationId={}", dto.receiverId(), dto, e);
      }
    }

    if(!userIds.isEmpty()){
      redisPublishListener.publish(new NotificationTarget(userIds));
    }
  }
}
