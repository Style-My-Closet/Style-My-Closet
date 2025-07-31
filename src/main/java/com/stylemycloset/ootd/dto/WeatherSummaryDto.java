package com.stylemycloset.ootd.dto;

import com.stylemycloset.ootd.tempEnum.SkyStatus;

public record WeatherSummaryDto(
    Long weatherId,
    SkyStatus skyStatus,
    PrecipitationDto precipitation,
    TemperatureDto temperature
) {

}
