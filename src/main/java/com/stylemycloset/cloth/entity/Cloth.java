package com.stylemycloset.cloth.entity;

import com.stylemycloset.binarycontent.BinaryContent;
import com.stylemycloset.common.entity.SoftDeletableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

import java.util.ArrayList;
import java.util.List;

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

  @OneToOne
  @JoinColumn(name = "image_id")
  private BinaryContent binaryContent;

  @Column(nullable = false, length = 100)
  private String name;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id", nullable = false)
  private ClothingCategory category;

  @OneToMany(mappedBy = "cloth", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @Where(clause = "deleted_at IS NULL")
  private List<ClothingAttributeValue> attributeValues = new ArrayList<>();

  private Cloth(String name, Closet closet, ClothingCategory category, BinaryContent binaryContent) {
    this.name = name;
    this.closet = closet;
    this.category = category;
    this.binaryContent = binaryContent;
  }

  public static Cloth createCloth(String name, Closet closet, ClothingCategory category, BinaryContent binaryContent) {
    Cloth cloth = new Cloth(name, closet, category, binaryContent);
    
    // 양방향 관계 자동 동기화
    if (closet != null) {
      closet.addCloth(cloth);
    }
    if (category != null) {
      category.addCloth(cloth);
    }
    
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


  public void updateBinaryContent(BinaryContent binaryContent) {
    if (binaryContent != null) {
      this.binaryContent = binaryContent;
    }
  }

  

  // Soft delete 수행
  public void softDeleteWithCleanup() {
    this.softDelete();
    this.attributeValues.clear();

    if (closet != null && closet.getClothes() != null) {
      closet.getClothes().remove(this);
    }
    if (category != null && category.getClothes() != null) {
      category.getClothes().remove(this);
    }
  }

  
}