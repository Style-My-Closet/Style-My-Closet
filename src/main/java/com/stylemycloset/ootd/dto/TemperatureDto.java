package com.stylemycloset.ootd.dto;

public record TemperatureDto(
    Double current,
    Double comparedToDayBefore,
    Double min,
    Double max
) {

}
