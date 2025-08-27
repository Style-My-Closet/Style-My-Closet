package com.stylemycloset.weather.entity;

import com.stylemycloset.location.Location;
import jakarta.persistence.*; // JPA 어노테이션 전반
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import lombok.*; // Lombok 어노테이션
import java.time.LocalDateTime; // 시간 관련
import com.stylemycloset.common.entity.CreatedAtEntity;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "weather")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder

public class Weather extends CreatedAtEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "weather_seq_gen")
  @SequenceGenerator(name = "weather_seq_gen", sequenceName = "weather_id_seq", allocationSize = 1)
  private Long id;

  // created_at에서 날짜만 잘라 저장
  @Column(name = "created_date", nullable = false)
  private LocalDate createdDate;

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
  @JdbcTypeCode(SqlTypes.VARCHAR)
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
  @Builder.Default
  private Boolean isAlertTriggered = false;
  //이 날씨 정보로 인해 알림이 트리거되었는지 여부

  @Enumerated(EnumType.STRING)
  @Column(name = "alert_type")
  private AlertType alertType;
  //알림이 발생했다면 그 타입 (예: 비, 폭우, 고온, 강풍 등)

  @Builder
  public Weather( Location location, SkyStatus skyStatus,
                 Precipitation precipitation, Temperature temperature,
                 Humidity humidity, WindSpeed windSpeed,
                 Boolean isAlertTriggered, AlertType alertType) {
    this.location = location;
    this.skyStatus = skyStatus;
    this.precipitation = precipitation;
    this.temperature = temperature;
    this.humidity = humidity;
    this.windSpeed = windSpeed;
    this.isAlertTriggered = alertTriggered();
    this.alertType = alertType;
  }

  public enum SkyStatus {
    CLEAR, MOSTLY_CLOUDY, CLOUDY
  }

  public enum AlertType {
    NONE, RAIN, HEAVY_RAIN,RAIN_SNOW ,SNOW, SHOWER,HIGH_TEMP, LOW_TEMP, STRONG_WIND
  }

  public boolean alertTriggered() {
    switch (this.alertType) {
      case RAIN, HEAVY_RAIN,HIGH_TEMP,LOW_TEMP,STRONG_WIND -> {
            return true;
        }
        default -> {
        return false;
      }
    }
  }


  @PrePersist
  public void prePersist() {
    if (createdAt == null) {
      createdAt = Instant.now();
    }
    createdDate = createdAt.atZone(ZoneId.systemDefault()).toLocalDate();
  }

  @PreUpdate
  public void preUpdate() {
    if (createdAt == null) {
      createdAt = Instant.now();
    }
    createdDate = createdAt.atZone(ZoneId.systemDefault()).toLocalDate();
  }

}