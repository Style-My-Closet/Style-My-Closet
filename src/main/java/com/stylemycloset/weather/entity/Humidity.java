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
public class Humidity {

  @Column(name = "humidity_current")
  private Double current;

  @Column(name = "humidity_compared_to_day_before")
  private Double comparedToDayBefore;

}
