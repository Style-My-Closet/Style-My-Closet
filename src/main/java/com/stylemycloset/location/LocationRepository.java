package com.stylemycloset.location;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {

    // 정확히 일치하는 위도/경도 값으로 조회
    Optional<Location> findByLatitudeAndLongitude(Double latitude, Double longitude);

    // 또는 범위 기반 조회 (실제 환경에서는 부동소수점 오차를 고려해 이 방식이 더 안전)
    Optional<Location> findFirstByLatitudeBetweenAndLongitudeBetween(
        Double latStart, Double latEnd,
        Double lonStart, Double lonEnd
    );
}
