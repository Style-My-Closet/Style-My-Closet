package com.stylemycloset.cloth.entity;


import com.stylemycloset.common.entity.SoftDeletableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "clothes_categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClothingCategory extends SoftDeletableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "clothes_categories_seq_gen")
  @SequenceGenerator(name = "clothes_categories_seq_gen", sequenceName = "clothes_categories_id_seq", allocationSize = 1)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private ClothingCategoryType name;

  

  public ClothingCategory(ClothingCategoryType name) {
    this.name = name;
  }




}