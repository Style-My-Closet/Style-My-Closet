package com.stylemycloset.cloth.entity;

import com.stylemycloset.common.entity.SoftDeletableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

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
  // 양방향 관계 동기화를 위한 팩토리 메서드
  public static ClothingAttributeValue createValue(Cloth cloth, ClothingAttribute attribute, AttributeOption option) {
    ClothingAttributeValue value = ClothingAttributeValue.builder()
            .cloth(cloth)
            .attribute(attribute)
            .option(option)
            .build();
    
    value.syncToCollections();
    return value;
  }

  // 컬렉션 동기화 헬퍼 메서드 (option 제거)
  private void syncToCollections() {
    addToCollectionIfLoaded(cloth, cloth != null ? cloth.getAttributeValues() : null);
    addToCollectionIfLoaded(attribute, attribute != null ? attribute.getAttributeValues() : null);
  }

  // Soft delete 시 관계 정리 (간소화)
  @Override
  public void softDelete() {
    super.softDelete();
    removeFromCollections();
  }

  // 컬렉션에서 제거하는 헬퍼 메서드 (option 제거)
  private void removeFromCollections() {
    removeFromCollectionIfLoaded(cloth, cloth != null ? cloth.getAttributeValues() : null);
    removeFromCollectionIfLoaded(attribute, attribute != null ? attribute.getAttributeValues() : null);
    // option.getAttributeValues() 제거됨 (단방향으로 개선)
  }

  // 간단한 컬렉션 추가 (JPA가 자동으로 Lazy Loading 처리)
  private void addToCollectionIfLoaded(Object entity, List<ClothingAttributeValue> collection) {
    if (entity != null && collection != null) {
      collection.add(this);
    }
  }

  // 간단한 컬렉션 제거 (JPA가 자동으로 Lazy Loading 처리)
  private void removeFromCollectionIfLoaded(Object entity, List<ClothingAttributeValue> collection) {
    if (entity != null && collection != null) {
      collection.remove(this);
    }
  }
}