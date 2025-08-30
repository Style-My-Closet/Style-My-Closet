package com.stylemycloset.user.dto.data;

import com.stylemycloset.location.Location;
import com.stylemycloset.user.entity.Gender;
import com.stylemycloset.weather.dto.WeatherAPILocation;
import java.time.LocalDate;

public record ProfileDto(
    Long userId,
    String name,
    Gender gender,
    LocalDate birthDate,
    WeatherAPILocation location,
    Integer temperatureSensitivity,
    String profileImageUrl
) {

}
