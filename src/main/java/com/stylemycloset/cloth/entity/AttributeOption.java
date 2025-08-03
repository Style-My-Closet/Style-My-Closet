package com.stylemycloset.cloth.entity;

import com.stylemycloset.common.entity.SoftDeletableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clothes_attributes_category_options")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AttributeOption extends SoftDeletableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "attribute_option_seq_gen")
  @SequenceGenerator(name = "attribute_option_seq_gen", sequenceName = "clothes_attributes_category_options_id_seq", allocationSize = 1)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "attribute_id", nullable = false)
  private ClothingAttribute attribute;

  @Column(nullable = false, length = 50)
  private String value;

  @OneToMany(mappedBy = "option", fetch = FetchType.LAZY)
  @Builder.Default
  private List<ClothingAttributeValue> attributeValues = new ArrayList<>();


  public static void createOption(ClothingAttribute attribute, String value) {
    AttributeOption option = AttributeOption.builder()
            .attribute(attribute)
            .value(value)
            .build();
    
    attribute.getOptions().add(option);

  }
  

  public boolean hasValueIn(List<String> values) {

    return values.contains(this.value);
  }
  
  public void setAttribute(ClothingAttribute attribute) {

    this.attribute = attribute;
  }

}
