package com.stylemycloset.clothes.entity.clothes;

import com.stylemycloset.clothes.entity.attribute.ClothesAttributeSelectableValue;
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
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

@Getter
@Entity
@Table(name = "clothes_attribute_selected_value")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Where(clause = "deleted_at IS NULL")
public class ClothesAttributeSelectedValue extends SoftDeletableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "clothes_attribute_selected_value_seq_gen")
  @SequenceGenerator(name = "clothes_attribute_selected_value_seq_gen", sequenceName = "clothes_attribute_selected_value_id_seq", allocationSize = 50)
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ClothesAttributeSelectedValue that)) {
      return false;
    }
    return Objects.equals(clothes, that.clothes)
        && Objects.equals(selectableValue, that.selectableValue);
  }

  @Override
  public int hashCode() {
    int result = clothes != null ? clothes.hashCode() : 0;
    result = 31 * result + (selectableValue != null ? selectableValue.hashCode() : 0);
    return result;
  }

}