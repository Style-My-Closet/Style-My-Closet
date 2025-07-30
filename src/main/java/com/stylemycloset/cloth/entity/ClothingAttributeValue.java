package com.stylemycloset.cloth.entity;

import com.stylemycloset.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "clothing_attribute_values")
@Getter
@NoArgsConstructor
public class ClothingAttributeValue extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long clothingValueId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "cloth_id", nullable = false)
  private Cloth cloth;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "attribute_id", nullable = false)
  private ClothingAttribute attribute;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "option_id", nullable = false)
  private AttributeOption option;



}