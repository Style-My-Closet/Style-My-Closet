package com.stylemycloset.cloth.entity;

import com.stylemycloset.common.entity.BaseTimeEntity;
import com.stylemycloset.common.entity.CreatedAtEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "clothes_to_attribute_options")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClothingAttributeValue extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "clothes_to_attribute_options_seq_gen")
  @SequenceGenerator(name = "clothes_to_attribute_options_seq_gen", sequenceName = "clothes_to_attribute_options_id_seq", allocationSize = 1)
  private Long id;

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