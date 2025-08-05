package com.stylemycloset.user.dto;

import java.time.LocalDate;
import com.stylemycloset.user.entity.Gender;

public record ProfileUpdateRequest(
    String name,
    String email,
    Gender gender,
    LocalDate birthDate,
    Integer temperatureSensitivity
) {
} 