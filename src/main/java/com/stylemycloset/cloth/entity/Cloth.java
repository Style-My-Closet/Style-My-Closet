package com.stylemycloset.cloth.entity;

import com.stylemycloset.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clothes")
@Getter
@NoArgsConstructor
class Cloth extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long clothId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "closet_id", nullable = false)
  private Closet closet;

  //   참조 값만 다루기 위해
  @Column(name = "binary_content")
  private long binaryContent;

  @Column(nullable = false, length = 100)
  private String name;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id", nullable = false)
  private ClothingCategory category;

  @OneToMany(mappedBy = "cloth", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<ClothingAttributeValue> attributeValues = new ArrayList<>();
}
