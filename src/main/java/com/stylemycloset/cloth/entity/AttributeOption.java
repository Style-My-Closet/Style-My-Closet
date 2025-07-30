package com.stylemycloset.cloth.entity;

import com.stylemycloset.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "attribute_options")
@Getter
@NoArgsConstructor
public class AttributeOption extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "option_id")
  private Long optionId;


  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "attribute_id", nullable = false)
  private ClothingAttribute attribute;


  @Column(nullable = false, length = 50)
  private String value;


  @OneToMany(mappedBy = "option", fetch = FetchType.LAZY)
  private List<ClothingAttributeValue> attributeValues = new ArrayList<>();


}
