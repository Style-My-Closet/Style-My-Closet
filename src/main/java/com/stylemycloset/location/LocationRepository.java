package com.stylemycloset.location;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LocationRepository extends JpaRepository<Location, Long> {

    // 정확히 일치하는 위도/경도 값으로 조회
    @Query(value = """
    SELECT *
    FROM locations l
    WHERE ABS(l.y- :y) < 0.01
      AND ABS(l.x - :x) < 0.01
    LIMIT 1
""", nativeQuery = true)
    Optional<Location> findByLatitudeAndLongitude(@Param("y") int y,
                                                  @Param("x") int x);

    // 또는 범위 기반 조회 (실제 환경에서는 부동소수점 오차를 고려해 이 방식이 더 안전)
    Optional<Location> findTopByOrderByCreatedAtDesc();
}
