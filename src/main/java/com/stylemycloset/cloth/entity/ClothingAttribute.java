package com.stylemycloset.cloth.entity;

import com.stylemycloset.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clothing_attributes")
@Getter
@NoArgsConstructor
public class ClothingAttribute extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long attributeId;

  @Column(nullable = false, length = 50)
  private String name;


  @OneToMany(mappedBy = "attribute", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<AttributeOption> options = new ArrayList<>();

  @
          OneToMany(mappedBy = "attribute", fetch = FetchType.LAZY)
  private List<ClothingAttributeValue> attributeValues = new ArrayList<>();


}
