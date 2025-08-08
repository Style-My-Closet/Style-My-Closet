package com.stylemycloset.location;


import com.stylemycloset.common.entity.BaseTimeEntity;
import com.stylemycloset.common.entity.CreatedAtEntity;
import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "locations")
public class Location extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "locations_seq_gen")
  @SequenceGenerator(
      name = "locations_seq_gen",
      sequenceName = "locations_id_seq", // 스키마 명까지 명시
      allocationSize = 1
  )
  private Long id;

  @Column(nullable = false)
  private Double latitude;

  @Column(nullable = false)
  private Double longitude;

  @Column(nullable = false)
  private Integer x;

  @Column(nullable = false)
  private Integer y;



  @Type(value = JsonType.class)
  @Column(name = "location_names", columnDefinition = "json", nullable = false)
  private List<String> locationNames;

  @Builder
  public Location(Double latitude, Double longitude, Integer x, Integer y,
      List<String> locationNames) {
    this.latitude = latitude;
    this.longitude = longitude;
    this.x = x;
    this.y = y;
    this.locationNames = locationNames;
  }

}
