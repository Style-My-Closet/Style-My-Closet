package com.stylemycloset.directmessage.entity;

import com.stylemycloset.common.entity.SoftDeletableEntity;
import com.stylemycloset.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "messages")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DirectMessage extends SoftDeletableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "messages_seq_gen")
  @SequenceGenerator(name = "messages_seq_gen", sequenceName = "messages_id_seq", allocationSize = 1)
  private Long id;

  @JoinColumn(name = "sender_id", nullable = false)
  @OneToOne
  private User sender;

  @JoinColumn(name = "receiver_id", nullable = false)
  @OneToOne
  private User receiver;

  @Column(name = "content", nullable = false)
  private String content;

  public DirectMessage(User sender, User receiver, String content) {
    this.sender = sender;
    this.receiver = receiver;
    this.content = content;
  }

}
