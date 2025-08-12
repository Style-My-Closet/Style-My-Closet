package com.stylemycloset.ootd.entity;

import com.stylemycloset.common.entity.CreatedAtEntity;
import com.stylemycloset.user.entity.User;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "feed_likes", uniqueConstraints = @UniqueConstraint(name = "uk_feed_like_user_feed",
    columnNames = {"user_id", "feed_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

  private FeedLike(User user, Feed feed) {
    this.user = user;
    this.feed = feed;
  }

  public static FeedLike createFeedLike(User user, Feed feed) {
    return new FeedLike(user, feed);
  }

}