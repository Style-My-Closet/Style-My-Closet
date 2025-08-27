package com.stylemycloset.notification.entity;

import com.stylemycloset.common.entity.CreatedAtEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notifications")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Notification extends CreatedAtEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notifications_seq")
  @SequenceGenerator(
      name = "notifications_seq",
      sequenceName = "notifications_id_seq",
      allocationSize = 50
  )
  private Long id;

  @Column(name = "receiver_id", nullable = false)
  private Long receiverId;

  @Column(nullable = false, length = 100)
  private String title;

  @Column(name = "content")
  private String content;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private NotificationLevel level;

  public Notification(Long receiverId, String title, String content, NotificationLevel level) {
    this.receiverId = receiverId;
    this.title = title;
    this.content = content;
    this.level = level;
  }

}
