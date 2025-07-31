package com.stylemycloset.common.entity;



import com.stylemycloset.common.util.StringListJsonConverter;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "location")
public class Location {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Double latitude;

  @Column(nullable = false)
  private Double longitude;

  @Column(nullable = false)
  private Integer x;

  @Column(nullable = false)
  private Integer y;

  @Convert(converter = StringListJsonConverter.class)
  @Column(name = "location_names", columnDefinition = "json", nullable = false)
  private List<String> locationNames;
}
