package com.stylemycloset.weather.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Precipitation {
    private String type; // RAIN, SNOW 등 → enum으로 바꿔도 OK
    private Double amount;
    private Double probability;
}