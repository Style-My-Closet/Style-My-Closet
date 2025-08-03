package com.stylemycloset.ootd.entity;

import com.stylemycloset.cloth.entity.Cloth;
import com.stylemycloset.common.entity.CreatedAtEntity;
import jakarta.persistence.*;
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
  @SequenceGenerator(name = "feed_ootd_clothes_seq_gen", sequenceName = "feed_ootd_clothes_id_seq", allocationSize = 1)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "feed_id", nullable = false)
  private Feed feed;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "clothes_id", nullable = false)
  private Cloth clothes;

}