package com.stylemycloset.notification.dto;

import com.stylemycloset.notification.entity.Notification;
import com.stylemycloset.notification.entity.NotificationLevel;
import java.time.Instant;
import java.util.List;
import lombok.Builder;

@Builder
public record NotificationDto(
    Long id,
    Instant createdAt,
    Long receiverId,
    String title,
    String content,
    NotificationLevel level
) {
  public static NotificationDto from(Notification notification) {
    return NotificationDto.builder()
      .id(notification.getId())
      .createdAt(notification.getCreatedAt())
      .receiverId(notification.getReceiver().getId())
      .title(notification.getTitle())
      .content(notification.getContent())
      .level(notification.getLevel())
      .build();
  }

  public static List<NotificationDto> fromList(List<Notification> notifications) {
    return notifications.stream()
      .map(NotificationDto::from)
      .toList();
  }
}
