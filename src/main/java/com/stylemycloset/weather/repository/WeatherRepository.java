package com.stylemycloset.weather.repository;

import com.stylemycloset.weather.entity.Weather;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface WeatherRepository extends JpaRepository<Weather, Long> {

    @Query("""
      SELECT w
      FROM Weather w
      JOIN FETCH w.location l
      WHERE l.latitude = :lat AND l.longitude = :lon
      AND w.forecastAt >= :now
      ORDER BY w.forecastAt ASC
    """)
    List<Weather> findTheNext4DaysByLocation(
        @Param("lat") double latitude,
        @Param("lon") double longitude,
        @Param("now") LocalDateTime now
    );


    @Query("SELECT w FROM Weather w "
        + "WHERE w.forecastedAt >= :start "
        + "AND w.forecastedAt <= :end")
    List<Weather> findWeathersByForecastedAtYesterday(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query(""" 
        SELECT w FROM Weather w
        WHERE w.location.latitude = :latitude
        AND w.location.longitude = :longitude
        ORDER BY w.forecastAt DESC
    """)
    List<Weather> findByLocation(@Param("latitude") double latitude, @Param("longitude") double longitude);
}
