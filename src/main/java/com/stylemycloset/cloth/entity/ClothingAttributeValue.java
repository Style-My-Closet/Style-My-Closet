package com.stylemycloset.cloth.entity;

import com.stylemycloset.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "clothes_to_attribute_options")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClothingAttributeValue extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long clothingValueId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cloth_id", nullable = false)
    private Cloth cloth;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_id", nullable = false)
    private ClothingAttribute attribute;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id", nullable = false)
    private AttributeOption option;



}