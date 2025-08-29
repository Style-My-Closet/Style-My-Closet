package com.stylemycloset.clothes.entity.clothes;

import com.stylemycloset.binarycontent.entity.BinaryContent;
import com.stylemycloset.clothes.entity.attribute.ClothesAttributeSelectableValue;
import com.stylemycloset.common.entity.SoftDeletableEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
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
@Table(name = "clothes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Where(clause = "deleted_at IS NULL")
public class Clothes extends SoftDeletableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "clothes_seq_gen")
  @SequenceGenerator(name = "clothes_seq_gen", sequenceName = "clothes_id_seq", allocationSize = 50)
  private Long id;

  @Column(name = "owner_id", nullable = false)
  private Long ownerId;

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @JoinColumn(name = "image_id")
  @OneToOne
  private BinaryContent image;

  @Enumerated(EnumType.STRING)
  @Column(name = "clothes_type", nullable = false)
  private ClothesType clothesType;

  @BatchSize(size = 100)
  @OneToMany(mappedBy = "clothes", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<ClothesAttributeSelectedValue> selectedValues = new ArrayList<>();

  public Clothes(
      Long ownerId,
      String name,
      BinaryContent image,
      String type,
      List<ClothesAttributeSelectableValue> selectableValues
  ) {
    this.ownerId = ownerId;
    this.name = name;
    this.image = image;
    this.clothesType = ClothesType.from(type);
    this.selectedValues = toSelectedValues(selectableValues);
  }

  public void update(
      String name,
      BinaryContent newImage,
      String type,
      List<ClothesAttributeSelectableValue> selectableValues
  ) {
    if (name != null && !name.isBlank()) {
      this.name = name;
    }
    if (newImage != null) {
      this.image = newImage;
    }
    if (type != null && !this.clothesType.name().equals(type)) {
      this.clothesType = ClothesType.from(type);
    }
    if (selectableValues != null) {
      Set<ClothesAttributeSelectedValue> existSelectables = new LinkedHashSet<>(
          this.selectedValues);
      Set<ClothesAttributeSelectedValue> newSelectables = new LinkedHashSet<>(
          toSelectedValues(selectableValues));
      removeOldValues(existSelectables, newSelectables);
      addNewValues(existSelectables, newSelectables);
    }
  }

  @Override
  public void softDelete() {
    super.softDelete();
    for (ClothesAttributeSelectedValue selectedValue : selectedValues) {
      selectedValue.softDelete();
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Clothes that)) {
      return false;
    }
    if (!Objects.equals(ownerId, that.ownerId)) {
      return false;
    }
    if (!Objects.equals(name, that.name)) {
      return false;
    }
    return clothesType == that.clothesType;
  }

  @Override
  public int hashCode() {
    int result = ownerId != null ? ownerId.hashCode() : 0;
    result = 31 * result + (name != null ? name.hashCode() : 0);
    result = 31 * result + (clothesType != null ? clothesType.hashCode() : 0);
    return result;
  }

  private void removeOldValues(
      Set<ClothesAttributeSelectedValue> existSelectables,
      Set<ClothesAttributeSelectedValue> newSelectables
  ) {
    Set<ClothesAttributeSelectedValue> oldValues = new LinkedHashSet<>(existSelectables);
    oldValues.removeAll(newSelectables);
    for (ClothesAttributeSelectedValue selectableValue : oldValues) {
      selectableValue.softDelete();
    }
    this.selectedValues.removeIf(oldValues::contains);
  }

  private void addNewValues(
      Set<ClothesAttributeSelectedValue> existSelectables,
      Set<ClothesAttributeSelectedValue> newSelectables
  ) {
    Set<ClothesAttributeSelectedValue> newValues = new LinkedHashSet<>(newSelectables);
    newValues.removeAll(existSelectables);
    this.selectedValues.addAll(newValues);
  }

  private List<ClothesAttributeSelectedValue> toSelectedValues(
      List<ClothesAttributeSelectableValue> selectableValues
  ) {
    if (selectableValues == null || selectableValues.isEmpty()) {
      return new ArrayList<>();
    }
    List<ClothesAttributeSelectedValue> values = new ArrayList<>(selectableValues.size());
    for (ClothesAttributeSelectableValue selectableValue : selectableValues) {
      values.add(new ClothesAttributeSelectedValue(this, selectableValue));
    }
    return values;
  }

}