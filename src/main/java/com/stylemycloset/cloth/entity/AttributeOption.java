package com.stylemycloset.cloth.entity;

import com.stylemycloset.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clothes_attributes_category_options")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AttributeOption extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "option_id", nullable = false)
    private Long optionId;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_id", nullable = false)
    private ClothingAttribute attribute;


    @Column(nullable = false, length = 50)
    private String value;


    @OneToMany(mappedBy = "option", fetch = FetchType.LAZY)
    private List<ClothingAttributeValue> attributeValues = new ArrayList<>();

}
