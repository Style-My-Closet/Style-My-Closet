//package com.stylemycloset.cloth.entity;
//
//@Entity
//@Table(name = "clothes")
//@Getter @Setter
//@NoArgsConstructor
//public class Cloth {
//  @Id
//  @GeneratedValue(strategy = GenerationType.IDENTITY)
//  private Long clothId;
//
//  @ManyToOne(fetch = FetchType.LAZY)
//  @JoinColumn(name = "closet_id", nullable = false)
//  private Closet closet;
//  // 바이너리 참조로 변경
//  @OneToOne(fetch = FetchType.LAZY)
//  @JoinColumn(name = "binaryContent_id")
//  private Long BinaryContentId;
//
//  @Column(nullable = false, length = 100)
//  private String name;
//
//  @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
//  private offsetDatetime createdAt;
//
//  @Column(name = "updated_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
//  private offsetDatetime updatedAt;
//
//  @ManyToOne(fetch = FetchType.LAZY)
//  @JoinColumn(name = "category_id", nullable = false)
//  private ClothingCategory category;
//
//  @OneToMany(mappedBy = "cloth", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//  private List<ClothingAttributeValue> attributeValues = new ArrayList<>();
//}
