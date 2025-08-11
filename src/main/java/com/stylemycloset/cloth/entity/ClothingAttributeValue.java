package com.stylemycloset.cloth.entity;

import com.stylemycloset.common.entity.SoftDeletableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "clothes_to_attribute_options")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClothingAttributeValue extends SoftDeletableEntity {

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

  @Builder
  public ClothingAttributeValue(Cloth cloth, ClothingAttribute attribute, AttributeOption option) {
    this.cloth = cloth;
    this.attribute = attribute;
    this.option = option;
  }
  // 동기화 메서드
  public static void createValue(Cloth cloth, ClothingAttribute attribute, AttributeOption option) {
    ClothingAttributeValue value = ClothingAttributeValue.builder()
            .cloth(cloth)
            .attribute(attribute)
            .option(option)
            .build();
    
    cloth.getAttributeValues().add(value);
    attribute.getAttributeValues().add(value);
    option.getAttributeValues().add(value);

  }



  // Soft delete 시 관계 정리
  @Override
  public void softDelete() {
    super.softDelete();
    // 관계된 엔티티들의 메모리상 컬렉션에서 제거
    if (cloth != null && cloth.getAttributeValues() != null) {
      cloth.getAttributeValues().remove(this);
    }
    if (attribute != null && attribute.getAttributeValues() != null) {
      attribute.getAttributeValues().remove(this);
    }
    if (option != null && option.getAttributeValues() != null) {
      option.getAttributeValues().remove(this);
    }
  }
}