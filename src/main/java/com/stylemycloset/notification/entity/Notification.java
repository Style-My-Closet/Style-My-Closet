package com.stylemycloset.notification.entity;

import com.stylemycloset.user.entity.User;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

@Entity
@Table(name = "notifications")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Notification{

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  @SequenceGenerator(
      name = "notification_seq",
      sequenceName = "notification_id_seq",
      allocationSize = 1
  )
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "receiver_id",
      nullable = false,
      foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
  )
  private User receiver;

  @Column(nullable = false, length = 100)
  private String title;

  private String content;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private NotficationLevel level;

  @CreatedDate
  @Column(updatable = false, nullable = false)
  private Instant createdAt;
  
  public Notification(User receiver, String title, String content, NotficationLevel level) {
    this.receiver = receiver;
    this.title = title;
    this.content = content;
    this.level = level;
  }
}
