package com.stylemycloset.weather.entity;

import com.stylemycloset.weather.entity.Weather.AlertType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Precipitation {

  @Column(name = "precipitation_type")
  private AlertType type; // RAIN, SNOW 등 → enum으로 바꿔도 OK

  @Column(name = "precipitation_amount")
  private Double amount;

  @Column(name = "precipitation_probability")
  private Double probability;

}