package com.stylemycloset.clothes.entity.clothes;

import com.stylemycloset.clothes.entity.attribute.ClothesAttributeSelectableValue;
import com.stylemycloset.clothes.entity.clothes.Clothes;
import com.stylemycloset.common.entity.SoftDeletableEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

@Getter
@Entity
@Table(name = "clothes_attribute_selected_value")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClothesAttributeSelectedValue extends SoftDeletableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "clothes_attribute_selected_value_seq_gen")
  @SequenceGenerator(name = "clothes_attribute_selected_value_seq_gen", sequenceName = "clothes_attribute_selected_value_id_seq", allocationSize = 1)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "clothes_id", nullable = false)
  private Clothes clothes;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "attribute_selectable_id", nullable = false)
  private ClothesAttributeSelectableValue selectableValue;

  public ClothesAttributeSelectedValue(
      Clothes clothes,
      ClothesAttributeSelectableValue option
  ) {
    this.clothes = clothes;
    this.selectableValue = option;
  }

}