package com.stylemycloset.cloth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clothing_categories")
@Getter
@NoArgsConstructor
public class ClothingCategory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long categoryId;

  @Column(nullable = false, length = 50)
  private String name;

  @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
  @JoinColumn(name = "cloth_id",nullable = false)
  private List<Cloth> clothes = new ArrayList<>();

}