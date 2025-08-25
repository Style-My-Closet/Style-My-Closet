package com.stylemycloset.clothes.entity.attribute;

import com.stylemycloset.common.entity.SoftDeletableEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "clothes_attribute_definition")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Where(clause = "deleted_at IS NULL")
@BatchSize(size = 100)
public class ClothesAttributeDefinition extends SoftDeletableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "clothes_attribute_definition_seq_gen")
  @SequenceGenerator(name = "clothes_attribute_definition_seq_gen", sequenceName = "clothes_attribute_definition_id_seq", allocationSize = 1)
  private Long id;

  @Column(name = "name", nullable = false, length = 50)
  private String name;

  @BatchSize(size = 50)
  @OneToMany(mappedBy = "definition", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<ClothesAttributeSelectableValue> selectableValues;

  public ClothesAttributeDefinition(
      String name,
      List<String> selectableValues
  ) {
    this.name = name;
    this.selectableValues = convertToSelectableValues(selectableValues);
  }

  public void update(String definitionName, List<String> selectableValues) {
    if (definitionName != null && !definitionName.isBlank() && !this.name.equals(
        definitionName)
    ) {
      this.name = definitionName;
    }
    if (selectableValues != null && !selectableValues.isEmpty()) {
      this.selectableValues = convertToSelectableValues(selectableValues);
    }
  }

  @Override
  public void softDelete() {
    super.softDelete();
    for (ClothesAttributeSelectableValue selectableValue : selectableValues) {
      selectableValue.softDelete();
    }
  }

  private List<ClothesAttributeSelectableValue> convertToSelectableValues(
      List<String> selectableValues
  ) {
    if (selectableValues == null) {
      return new ArrayList<>();
    }
    List<ClothesAttributeSelectableValue> values = new ArrayList<>();
    for (String selectableValue : selectableValues) {
      values.add(new ClothesAttributeSelectableValue(this, selectableValue));
    }
    return values;
  }

}
