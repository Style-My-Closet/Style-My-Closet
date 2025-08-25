package com.stylemycloset.recommendation.entity;


import com.stylemycloset.common.util.VectorType;
import com.stylemycloset.user.entity.Gender;
import com.stylemycloset.weather.entity.Weather.AlertType;
import com.stylemycloset.weather.entity.Weather.SkyStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;


@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "clothing_conditions")
public class ClothingCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double temperature;

    private double windSpeed;

    private double humidity;

    @Enumerated(EnumType.ORDINAL)
    private Gender gender;

    private Integer temperatureSensitivity;

    @Enumerated(EnumType.ORDINAL)
    private SkyStatus skyStatus;

    @Column(length = 50)
    @Enumerated(EnumType.ORDINAL)
    private AlertType weatherType;

    @Enumerated(EnumType.ORDINAL)
    private Color color;

    @Enumerated(EnumType.ORDINAL)
    private SleeveLength sleeveLength;

    @Enumerated(EnumType.ORDINAL)
    private PantsLength pantsLength;

    // 추천 여부 (1=추천, 0=비추천)
    private Boolean label;

    @Type(VectorType.class)
    @Column(columnDefinition = "vector(28)")
    private float[] embedding;


}
