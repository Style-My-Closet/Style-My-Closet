package com.stylemycloset.weather.entity;

import com.stylemycloset.common.entity.Location;
import jakarta.persistence.*; // JPA 어노테이션 전반
import lombok.*; // Lombok 어노테이션
import java.time.LocalDateTime; // 시간 관련
import java.util.UUID; // UUID 타입
import com.stylemycloset.common.entity.BaseEntity;

@Entity
@Table(name = "weather")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder

public class Weather extends BaseEntity {
  //weather쪽에서는 id가 uuid타입이여서 baseentity 상속받으면 문제 생길것 같습니다!
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "weather_seq_gen")
  @SequenceGenerator(name = "weather_seq_gen", sequenceName = "weather_seq", allocationSize = 1)
  private Long id;

  @Column(name = "forecasted_at", nullable = false)
  private LocalDateTime forecastedAt;

  @Column(name = "forecast_at", nullable = false)
  private LocalDateTime forecastAt;

  //forecastedAt: 예보가 생성된 시점 (기상청이 예보 발표한 시점)

  //forecastAt: 실제 적용되는 시점 (해당 예보가 유효한 시간)

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "location_id", nullable = false)
  private Location location;
  /*위치 정보  */

  @Enumerated(EnumType.STRING)
  @Column(name = "sky_status", nullable = false)
  private SkyStatus skyStatus;
  // 하늘 상태 (맑음, 흐림 등)

  @Embedded
  private Precipitation precipitation;
  //강수 확률

  @Embedded
  private Temperature temperature;
  //온도

  @Embedded
  private Humidity humidity;
  //습도

  @Embedded
  private WindSpeed windSpeed;
  //풍속


  // 알림 관련
  @Column(name = "is_alert_triggered")
  private Boolean isAlertTriggered = false;
  //이 날씨 정보로 인해 알림이 트리거되었는지 여부

  @Enumerated(EnumType.STRING)
  @Column(name = "alert_type")
  private AlertType alertType;
  //알림이 발생했다면 그 타입 (예: 비, 폭우, 고온, 강풍 등)

  @Builder
  public Weather(LocalDateTime forecastedAt, LocalDateTime forecastAt,
                 Location location, SkyStatus skyStatus,
                 Precipitation precipitation, Temperature temperature,
                 Humidity humidity, WindSpeed windSpeed,
                 Boolean isAlertTriggered, AlertType alertType) {
    this.forecastedAt = forecastedAt;
    this.forecastAt = forecastAt;
    this.location = location;
    this.skyStatus = skyStatus;
    this.precipitation = precipitation;
    this.temperature = temperature;
    this.humidity = humidity;
    this.windSpeed = windSpeed;
    this.isAlertTriggered = isAlertTriggered;
    this.alertType = alertType;
  }

  public enum SkyStatus {
    CLEAR, MOSTLY_CLOUDY, CLOUDY
  }

  public enum AlertType {
    NONE, RAIN, HEAVY_RAIN, HIGH_TEMP, LOW_TEMP, STRONG_WIND
  }


}