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
import jakarta.persistence.OrderBy;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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
  @SequenceGenerator(name = "clothes_attribute_definition_seq_gen", sequenceName = "clothes_attribute_definition_id_seq", allocationSize = 50)
  private Long id;

  @Column(name = "name", nullable = false, length = 50)
  private String name;

  @BatchSize(size = 50)
  @OrderBy("id ASC")
  @OneToMany(mappedBy = "definition", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<ClothesAttributeSelectableValue> selectableValues;

  public ClothesAttributeDefinition(
      String name,
      List<String> selectableValues
  ) {
    this.name = name;
    this.selectableValues = toSelectableValues(selectableValues);
  }

  public void update(String definitionName, List<String> rawSelectableValues) {
    if (definitionName != null && !definitionName.isBlank() && !this.name.equals(
        definitionName)
    ) {
      this.name = definitionName;
    }
    if (rawSelectableValues != null) {
      Set<ClothesAttributeSelectableValue> existSelectables = new LinkedHashSet<>(
          this.selectableValues);
      Set<ClothesAttributeSelectableValue> newSelectables = new LinkedHashSet<>(
          toSelectableValues(rawSelectableValues));
      removeOldValues(existSelectables, newSelectables);
      addNewValues(existSelectables, newSelectables);
    }
  }

  @Override
  public void softDelete() {
    super.softDelete();
    for (ClothesAttributeSelectableValue selectableValue : selectableValues) {
      selectableValue.softDelete();
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o instanceof ClothesAttributeDefinition that) {
      return Objects.equals(this.name, that.name);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return name != null ? name.hashCode() : 0;
  }

  private void removeOldValues(
      Set<ClothesAttributeSelectableValue> existSelectables,
      Set<ClothesAttributeSelectableValue> newSelectables
  ) {
    Set<ClothesAttributeSelectableValue> oldValues = new LinkedHashSet<>(existSelectables);
    oldValues.removeAll(newSelectables);
    for (ClothesAttributeSelectableValue selectableValue : oldValues) {
      selectableValue.softDelete();
    }
    this.selectableValues.removeIf(oldValues::contains);
  }

  private void addNewValues(
      Set<ClothesAttributeSelectableValue> existSelectables,
      Set<ClothesAttributeSelectableValue> newSelectables
  ) {
    Set<ClothesAttributeSelectableValue> newValues = new LinkedHashSet<>(newSelectables);
    newValues.removeAll(existSelectables);
    this.selectableValues.addAll(newValues);
  }

  private List<ClothesAttributeSelectableValue> toSelectableValues(
      List<String> selectableValues
  ) {
    if (selectableValues == null || selectableValues.isEmpty()) {
      return new ArrayList<>();
    }
    List<ClothesAttributeSelectableValue> values = new ArrayList<>();
    for (String selectableValue : selectableValues) {
      values.add(new ClothesAttributeSelectableValue(this, selectableValue));
    }
    return values;
  }

}
