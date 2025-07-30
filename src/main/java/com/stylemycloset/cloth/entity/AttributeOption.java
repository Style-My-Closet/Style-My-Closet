//package com.stylemycloset.cloth.entity;
//
//@Entity
//@Table(name = "attribute_options")
//@Getter @Setter
//@NoArgsConstructor
//public class AttributeOption {
//
//  @Id
//  @GeneratedValue(strategy = GenerationType.IDENTITY)
//  @Column(name = "option_id")
//  private Long optionId;
//
//
//  @ManyToOne(fetch = FetchType.LAZY)
//  @JoinColumn(name = "attribute_id", nullable = false)
//  private ClothingAttribute attribute;
//
//
//  @Column(nullable = false, length = 50)
//  private String value;
//
//
//  @OneToMany(mappedBy = "option", fetch = FetchType.LAZY)
//  private List<ClothingAttributeValue> attributeValues = new ArrayList<>();
//
//  @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
//  private offsetDatetime createdAt;
//
//  @Column(name = "updated_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
//  private offsetDatetime updatedAt;
//}
