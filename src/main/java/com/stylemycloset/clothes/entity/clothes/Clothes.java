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
import java.util.List;
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
  @SequenceGenerator(name = "clothes_seq_gen", sequenceName = "clothes_id_seq", allocationSize = 1)
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
    this.selectedValues = convertToSelectedValues(selectableValues);
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
    if (selectableValues != null && !selectableValues.isEmpty()) {
      this.selectedValues = convertToSelectedValues(selectableValues);
    }
  }

  @Override
  public void softDelete() {
    super.softDelete();
    for (ClothesAttributeSelectedValue selectedValue : selectedValues) {
      selectedValue.softDelete();
    }
  }

  private List<ClothesAttributeSelectedValue> convertToSelectedValues(
      List<ClothesAttributeSelectableValue> selectableValues
  ) {
    if (selectableValues == null || selectableValues.isEmpty()) {
      return new ArrayList<>();
    }
    List<ClothesAttributeSelectedValue> list = new ArrayList<>(selectableValues.size());
    for (ClothesAttributeSelectableValue sv : selectableValues) {
      list.add(new ClothesAttributeSelectedValue(this, sv));
    }
    return list;
  }

}