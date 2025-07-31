package com.stylemycloset.weather.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Temperature {

  @Column(name = "temperature_current")
  private Double current;

  @Column(name = "temperature_compared_to_day_before")
  private Double comparedToDayBefore;

  @Column(name = "temperature_min")
  private Double min;

  @Column(name = "temperature_max")
  private Double max;

}