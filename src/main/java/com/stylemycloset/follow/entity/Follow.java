package com.stylemycloset.follow.entity;

import com.stylemycloset.common.entity.SoftDeleteEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.stylemycloset.user.entity.User;

import java.time.Instant;

@Getter
@Entity
@Table(name = "follows")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Follow extends SoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "follows_seq_gen")
    @SequenceGenerator(name = "follows_seq_gen", sequenceName = "follows_id_seq", allocationSize = 1)
    private Long id;

    @JoinColumn(name = "follower_id", nullable = false)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private User follower;

    @JoinColumn(name = "followee_id", nullable = false)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private User followee;

    @Column(name = "followed_at", nullable = false)
    private Instant followedAt;

}