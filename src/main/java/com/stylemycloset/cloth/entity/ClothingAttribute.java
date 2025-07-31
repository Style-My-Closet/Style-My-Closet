package com.stylemycloset.cloth.entity;

import com.stylemycloset.common.entity.SoftDeletableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import com.stylemycloset.common.entity.CreatedAtEntity;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clothes_attributes_categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClothingAttribute extends SoftDeletableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "clothes_attributes_categories_seq_gen")
  @SequenceGenerator(name = "clothes_attributes_categories_seq_gen", sequenceName = "clothes_attributes_categories_id_seq", allocationSize = 1)
  private Long id;

  @Column(nullable = false, length = 50)
  private String name;

  @OneToMany(mappedBy = "attribute", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<AttributeOption> options = new ArrayList<>();

  @OneToMany(mappedBy = "attribute", fetch = FetchType.LAZY)
  private List<ClothingAttributeValue> attributeValues = new ArrayList<>();

}
