package com.stylemycloset.ootd.entity;

import com.stylemycloset.cloth.entity.Cloth;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "feed_ootd_clothes", uniqueConstraints = @UniqueConstraint(columnNames = {"feed_id", "clothes_id"}))
@Getter
@NoArgsConstructor
public class FeedClothes {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_id", nullable = false)
    private Feed feed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clothes_id", nullable = false)
    private Cloth clothes;
}