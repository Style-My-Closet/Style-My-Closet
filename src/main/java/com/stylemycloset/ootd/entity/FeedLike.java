package com.stylemycloset.ootd.entity;

import com.stylemycloset.common.entity.CreatedAtEntity;
import com.stylemycloset.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "feed_likes")
@Getter
@NoArgsConstructor
public class FeedLike extends CreatedAtEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "feed_likes_seq_gen")
  @SequenceGenerator(name = "feed_likes_seq_gen", sequenceName = "feed_likes_id_seq", allocationSize = 1)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "feed_id", nullable = false)
  private Feed feed;

}