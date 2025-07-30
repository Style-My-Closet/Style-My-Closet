//package com.stylemycloset.cloth;
//
//@Entity
//@Table(name = "clothing_categories")
//@Getter @Setter
//@NoArgsConstructor
//public class ClothingCategory {
//
//  @Id
//  @GeneratedValue(strategy = GenerationType.IDENTITY)
//  private Long categoryId;
//
//  @Column(nullable = false, length = 50)
//  private String name;
//
//  @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
//  private List<Cloth> clothes = new ArrayList<>();
//
//}