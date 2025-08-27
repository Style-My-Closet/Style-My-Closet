package com.stylemycloset.ootd.entity;

import com.stylemycloset.clothes.entity.clothes.Clothes;
import com.stylemycloset.common.entity.CreatedAtEntity;
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
@Table(name = "feed_ootd_clothes", uniqueConstraints = @UniqueConstraint(columnNames = {"feed_id",
    "clothes_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedClothes extends CreatedAtEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "feed_ootd_clothes_seq_gen")
  @SequenceGenerator(name = "feed_ootd_clothes_seq_gen", sequenceName = "feed_ootd_clothes_id_seq", allocationSize = 50)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "feed_id", nullable = false)
  private Feed feed;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "clothes_id", nullable = false)
  private Clothes clothes;

  private FeedClothes(Feed feed, Clothes clothes) {
    this.feed = feed;
    this.clothes = clothes;
  }

  public static FeedClothes createFeedClothes(Feed feed, Clothes clothes) {
    return new FeedClothes(feed, clothes);
  }

}