package com.stylemycloset.cloth.entity;

import com.stylemycloset.common.entity.SoftDeletableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "clothes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Where(clause = "deleted_at IS NULL")
public class Cloth extends SoftDeletableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "clothes_seq_gen")
  @SequenceGenerator(name = "clothes_seq_gen", sequenceName = "clothes_id_seq", allocationSize = 1)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "closet_id", nullable = false)
  private Closet closet;

  @Column(name = "image_id")
  private UUID binaryContentId;

  @Column(nullable = false, length = 100)
  private String name;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id", nullable = false)
  private ClothingCategory category;

  @OneToMany(mappedBy = "cloth", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @Where(clause = "deleted_at IS NULL")
  private List<ClothingAttributeValue> attributeValues = new ArrayList<>();

  private Cloth(String name, Closet closet, ClothingCategory category, UUID binaryContentId) {
    this.name = name;
    this.closet = closet;
    this.category = category;
    this.binaryContentId = binaryContentId;
  }

  public static Cloth createCloth(String name, Closet closet, ClothingCategory category, java.util.UUID binaryContentId) {
    Cloth cloth = new Cloth(name, closet, category, binaryContentId);
 
    
    return cloth;
  }

  // 속성값 추가
  public void addAttributeValue(ClothingAttribute attribute, AttributeOption option) {
    ClothingAttributeValue.createValue(this, attribute, option);
  }
  


  public void updateName(String name) {
    if (name == null || name.isBlank()) return;
    this.name = name;
  }


  public void updateBinaryContentId(java.util.UUID binaryContentId) {
    if (binaryContentId != null) {
      this.binaryContentId = binaryContentId;
    }
  }

  // 양방향 연관관계 편의 메서드
  public void setCloset(Closet closet) {
    this.closet = closet;
  }

  public void setCategory(ClothingCategory category) {
    this.category = category;
  }

  

  // Soft delete 수행
  public void softDeleteWithCleanup() {
    this.softDelete();
    this.attributeValues.clear();

    if (closet != null && closet.getClothes() != null) {
      closet.getClothes().remove(this);
    }
  }

  
}