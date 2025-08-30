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
public class WindSpeed {

  @Column(name = "wind_speed_current")
  private Double current;

  @Column(name = "wind_speed_compared_to_day_before")
  private Double comparedToDayBefore;

  public String mapWindStrength(Double speed) {
    if (speed == null) {
      return "UNKNOWN";
    } else if (speed < 3.4) {
      return "WEAK";
    } else if (speed < 6.7) {
      return "MODERATE";
    } else {
      return "STRONG";
    }
  }
}