package com.stylemycloset.recommendation.entity;


import com.stylemycloset.common.util.VectorType;
import com.stylemycloset.user.entity.Gender;
import com.stylemycloset.weather.entity.Weather.AlertType;
import com.stylemycloset.weather.entity.Weather.SkyStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;



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
    private Length length;

    @Enumerated(EnumType.ORDINAL)
    private Material material;

    // 추천 여부 (1=추천, 0=비추천)
    private Boolean label;

    @Type(VectorType.class)
    @JdbcTypeCode(SqlTypes.OTHER)
    @Column(columnDefinition = "vector(37)")
    private float[] embedding;


}
