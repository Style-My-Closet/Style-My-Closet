package com.stylemycloset.recommendation.entity;

import com.stylemycloset.cloth.entity.AttributeOption;
import com.stylemycloset.cloth.entity.Cloth;
import com.stylemycloset.cloth.entity.ClothingAttribute;
import com.stylemycloset.cloth.entity.ClothingAttributeValue;
import com.stylemycloset.user.entity.Gender;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "clothing_feature")
public class ClothingFeature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double temperature;

    private double windSpeed;

    private double humidity;

    private Gender gender;

    private Integer temperatureSensitivity;

    @ManyToOne(fetch = FetchType.LAZY)

    private ClothingAttributeValue clothOption;

    @Column(length = 50)
    private String weatherType;

    // 추천 여부 (1=추천, 0=비추천)
    private Boolean label;
}
