package com.stylemycloset.cloth.entity;

import com.stylemycloset.common.entity.SoftDeletableEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clothes_attributes_categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ClothingAttribute extends SoftDeletableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "clothes_attributes_categories_seq_gen")
  @SequenceGenerator(name = "clothes_attributes_categories_seq_gen", sequenceName = "clothes_attributes_categories_id_seq", allocationSize = 1)
  private Long id;

  @Column(nullable = false, length = 50)
  private String name;

  @OneToMany(mappedBy = "attribute", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
  @BatchSize(size = 10) // N+1 문제 해결: 한 번에 10개씩 배치로 로딩
  @Builder.Default
  private List<AttributeOption> options = new ArrayList<>();

  @OneToMany(mappedBy = "attribute", fetch = FetchType.LAZY)
  @Builder.Default
  private List<ClothingAttributeValue> attributeValues = new ArrayList<>();

  // 사용자 정의 생성자 (빌더 패턴에서 사용하지 않음)
  public static ClothingAttribute createWithOptions(String name, List<String> selectableValues) {
    ClothingAttribute attribute = ClothingAttribute.builder()
            .name(name)
            .build();
            
    selectableValues.forEach(v -> AttributeOption.createOption(attribute, v));
    return attribute;
  }



  // 업데이트 메서드 (Soft Delete 방식으로 변경)
  public void updateAttribute(String name, List<String> selectableValues) {
    this.name = name;
    
    // 현재 활성 옵션들 중 새 목록에 없는 것들은 soft delete
    List<String> currentActiveValues = getActiveOptions().stream()
            .map(AttributeOption::getValue)
            .toList();
    List<String> removingValues = currentActiveValues.stream()
            .filter(value -> !selectableValues.contains(value))
            .toList();
    
    if (!removingValues.isEmpty()) {
      removeOptions(removingValues);
    }
    
    // 새로운 옵션들 중 기존에 없는 것들만 추가
    List<String> newValues = selectableValues.stream()
            .filter(value -> !currentActiveValues.contains(value))
            .toList();
    
    if (!newValues.isEmpty()) {
      addOptions(newValues);
    }
  }


  public void updateName(String name) {
    this.name = name;
  }
  
  // 옵션 추가 (AttributeOption에게 생성 책임 위임)
  public void addOptions(List<String> values) {
    values.forEach(value -> {
      AttributeOption.createOption(this, value);  // 양방향 동기화 자동 처리됨
    });
  }
  
  // 옵션 제거 (실제 삭제 대신 soft delete 적용)
  public void removeOptions(List<String> values) {
    this.options.stream()
            .filter(option -> option.hasValueIn(values))
            .forEach(AttributeOption::softDelete);
  }
  
  // 삭제되지 않은 활성 옵션들만 반환
  public List<AttributeOption> getActiveOptions() {
    return options.stream()
            .filter(option -> !option.isDeleted())
            .toList();
  }

}
