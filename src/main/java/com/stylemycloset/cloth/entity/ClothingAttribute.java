package com.stylemycloset.cloth.entity;

import com.stylemycloset.common.entity.SoftDeletableEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Where;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "clothes_attributes_categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Where(clause = "deleted_at IS NULL")
public class ClothingAttribute extends SoftDeletableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "clothes_attributes_categories_seq_gen")
  @SequenceGenerator(name = "clothes_attributes_categories_seq_gen", sequenceName = "clothes_attributes_categories_id_seq", allocationSize = 1)
  private Long id;

  @Column(nullable = false, length = 50)
  private String name;

  @OneToMany(mappedBy = "attribute", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
  @Where(clause = "deleted_at IS NULL")
  @BatchSize(size = 10)
  @Builder.Default
  private List<AttributeOption> options = new ArrayList<>();

  @OneToMany(mappedBy = "attribute", fetch = FetchType.LAZY)
  @Where(clause = "deleted_at IS NULL")
  @Builder.Default
  private List<ClothingAttributeValue> attributeValues = new ArrayList<>();


  public static ClothingAttribute createWithOptions(String name, List<String> selectableValues) {
    ClothingAttribute attribute = ClothingAttribute.builder()
            .name(name)
            .build();
    // 옵션 생성 시 자동으로 더하도록함
    if (selectableValues != null) {
      selectableValues.forEach(v -> {
        if (v != null && !v.isBlank()) {
          AttributeOption.createOption(attribute, v);
        }
      });
    }
    return attribute;
  }






  public void updateName(String name) {
    this.name = name;
  }
  
  // 옵션 추가
  public void addOptions(List<String> values) {
    // 이미 존재하는 활성 옵션은 넘어가기
    Set<String> existingActiveValues = this.getActiveOptions().stream()
        .map(AttributeOption::getValue)
        .collect(java.util.stream.Collectors.toSet());

    values.forEach(value -> {
      if (value == null) {
        return;
      }
      if (existingActiveValues.contains(value)) {
        return;
      }
      AttributeOption.createOption(this, value);
      existingActiveValues.add(value);
    });
  }

  

  public void removeOptions(List<String> values) {
    if (values == null || values.isEmpty()) {
      return;
    }
    Set<String> valueSet = new HashSet<>();
    for (String v : values) {
      if (v != null && !v.isBlank()) valueSet.add(v);
    }
    if (valueSet.isEmpty()) return;

    List<AttributeOption> copy = new ArrayList<>(this.options);
    copy.stream()
        .filter(option -> valueSet.contains(option.getValue()))
        .forEach(AttributeOption::softDelete);
  }
  
  // 삭제되지 않은 활성 옵션들만 반환
  public List<AttributeOption> getActiveOptions() {
    return options.stream()
            .filter(option -> !option.isSoftDeleted())
            .toList();
  }

  // 메모리상 같이 처리
  public void softDeleteWithCleanup() {
    this.softDelete();
    List<AttributeOption> copy = new ArrayList<>(this.options);
    copy.forEach(AttributeOption::softDelete);
    this.options.clear();
    this.attributeValues.clear();
  }

  

  // 옵션들을 제거하고 메모리상에서도 삭제된 것들을 정리
  public void removeOptionsWithCleanup(List<String> values) {
    this.removeOptions(values);
    this.options.removeIf(AttributeOption::isSoftDeleted);
  }

}
