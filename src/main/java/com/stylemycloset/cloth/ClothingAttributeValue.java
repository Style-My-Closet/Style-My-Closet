//package com.stylemycloset.cloth;
//
//@Entity
//@Table(name = "clothing_attribute_values")
//@Getter @Setter
//@NoArgsConstructor
//public class ClothingAttributeValue {
//  @Id
//  @GeneratedValue(strategy = GenerationType.IDENTITY)
//  private Long clothingValueId;
//
//  @ManyToOne(fetch = FetchType.LAZY)
//  @JoinColpackage com.stylemycloset.cloth;
////
////@Entity
////@Table(name = "clothing_attribute_values")
////@Getter @Setter
////@NoArgsConstructor
////public class ClothingAttributeValue {
////  @Id
////  @GeneratedValue(strategy = GenerationType.IDENTITY)
////  private Long clothingValueId;
////
////  @ManyToOne(fetch = FetchType.LAZY)
////  @JoinColumn(name = "cloth_id", nullable = false)
////  private Cloth cloth;
////
////  @ManyToOne(fetch = FetchType.LAZY)
////  @JoinColumn(name = "attribute_id", nullable = false)
////  private ClothingAttribute attribute;
////
////  @ManyToOne(fetch = FetchType.LAZY)
////  @JoinColumn(name = "option_id", nullable = false)
////  private AttributeOption option;
////
////  @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
////  private offsetDatetime createdAt;
////
////  @Column(name = "updated_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
////  private offsetDatetime updatedAt;
////
////}umn(name = "cloth_id", nullable = false)
//  private Cloth cloth;
//
//  @ManyToOne(fetch = FetchType.LAZY)
//  @JoinColumn(name = "attribute_id", nullable = false)
//  private ClothingAttribute attribute;
//
//  @ManyToOne(fetch = FetchType.LAZY)
//  @JoinColumn(name = "option_id", nullable = false)
//  private AttributeOption option;
//
//  @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
//  private offsetDatetime createdAt;
//
//  @Column(name = "updated_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
//  private offsetDatetime updatedAt;
//
//}