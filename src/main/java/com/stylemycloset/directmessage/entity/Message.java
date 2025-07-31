package com.stylemycloset.directmessage.entity;

import com.stylemycloset.common.entity.SoftDeleteEntity;
import com.stylemycloset.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Entity
@Table(name = "messages")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Message extends SoftDeleteEntity {

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

    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;

}
