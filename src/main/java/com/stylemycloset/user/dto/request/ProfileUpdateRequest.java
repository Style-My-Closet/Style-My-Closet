package com.stylemycloset.user.dto.request;

import com.stylemycloset.location.Location;
import com.stylemycloset.user.entity.Gender;
import java.time.LocalDate;

public record ProfileUpdateRequest(
    String name,
    Gender gender,
    LocalDate birthDate,
    Location location,
    Integer temperatureSensitivity
) {

}

