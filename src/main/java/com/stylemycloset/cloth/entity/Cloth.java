package com.stylemycloset.cloth.entity;

import com.stylemycloset.binarycontent.BinaryContent;
import com.stylemycloset.common.entity.SoftDeletableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clothes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class Cloth extends SoftDeletableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "clothes_seq_gen")
  @SequenceGenerator(name = "clothes_seq_gen", sequenceName = "clothes_id_seq", allocationSize = 1)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "closet_id", nullable = false)
  private Closet closet;

  @OneToOne
  @JoinColumn(name = "image_id")
  private BinaryContent binaryContent;

  @Column(nullable = false, length = 100)
  private String name;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id", nullable = false)
  private ClothingCategory category;

  @OneToMany(mappedBy = "cloth", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @Builder.Default
  private List<ClothingAttributeValue> attributeValues = new ArrayList<>();
}