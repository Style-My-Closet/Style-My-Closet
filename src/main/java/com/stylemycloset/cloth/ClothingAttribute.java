//package com.stylemycloset.cloth;
//
//@Entity
//@Table(name = "clothing_attributes")
//@Getter @Setter
//@NoArgsConstructor
//public class ClothingAttribute {
//  @Id
//  @GeneratedValue(strategy = GenerationType.IDENTITY)
//  private Long attributeId;
//
//  @Column(nullable = false, length = 50)
//  private String name;
//
//  @CreationTimestamp
//  private LocalDateTime createdAt;
//  @OneToMany(mappedBy = "attribute", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//  private List<AttributeOption> options = new ArrayList<>();
//
//  @OneToMany(mappedBy = "attribute", fetch = FetchType.LAZY)
//  private List<ClothingAttributeValue> attributeValues = new ArrayList<>();
//
//  @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
//  private offsetDatetime createdAt;
//
//  @Column(name = "updated_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
//  private offsetDatetime updatedAt;
//}
