package com.stylemycloset.clothes.entity.attribute;

import com.stylemycloset.common.entity.SoftDeletableEntity;
import jakarta.persistence.Column;
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
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "clothes_attribute_selectable_value")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Where(clause = "deleted_at IS NULL")
@BatchSize(size = 100)
public class ClothesAttributeSelectableValue extends SoftDeletableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "clothes_attribute_selectable_value_seq_gen")
  @SequenceGenerator(name = "clothes_attribute_selectable_value_seq_gen", sequenceName = "clothes_attribute_selectable_value_id_seq", allocationSize = 1)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "attribute_definition_id", nullable = false)
  private ClothesAttributeDefinition definition;

  @Column(nullable = false, length = 50)
  private String value;

  public ClothesAttributeSelectableValue(ClothesAttributeDefinition definition, String value) {
    this.definition = definition;
    this.value = value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o instanceof ClothesAttributeSelectableValue that
        && Objects.equals(this.definition, that.definition)
    ) {
      return Objects.equals(this.value, that.value);
    }

    return false;
  }

  @Override
  public int hashCode() {
    int result = definition != null ? definition.hashCode() : 0;
    result = 31 * result + (value != null ? value.hashCode() : 0);
    return result;
  }

}
