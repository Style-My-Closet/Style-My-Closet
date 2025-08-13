package com.stylemycloset.recommendation.entity;

import com.stylemycloset.user.entity.Gender;
import com.stylemycloset.weather.entity.Weather.AlertType;
import com.stylemycloset.weather.entity.Weather.SkyStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "clothing_feature")
public class ClothingCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double temperature;

    private double windSpeed;

    private double humidity;

    private Gender gender;

    private Integer temperatureSensitivity;

    private SkyStatus skyStatus;

    @Column(length = 50)
    private AlertType weatherType;

    private Color color;

    private SleeveLength SleeveLength;

    private PantsLength pantsLength;

    // 추천 여부 (1=추천, 0=비추천)
    private Boolean label;
}
