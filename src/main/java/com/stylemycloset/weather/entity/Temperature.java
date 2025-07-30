package com.stylemycloset.weather.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Temperature {
    private Double current;
    private Double comparedToDayBefore;
    private Double min;
    private Double max;
}