package com.stylemycloset.weather.repository;

import com.stylemycloset.weather.entity.Weather;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WeatherRepository extends JpaRepository<Weather, Long> {

    @Query("""
        SELECT w FROM Weather w
        WHERE w.location.latitude = :latitude AND w.location.longitude = :longitude
        ORDER BY w.forecastAt DESC
    """)
    List<Weather> findByLocation(@Param("latitude") double latitude, @Param("longitude") double longitude);
}
