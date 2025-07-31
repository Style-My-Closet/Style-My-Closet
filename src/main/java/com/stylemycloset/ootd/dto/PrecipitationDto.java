package com.stylemycloset.ootd.dto;

import com.stylemycloset.ootd.tempEnum.PrecipitationType;

public record PrecipitationDto(
    PrecipitationType type,
    Double amount,
    Double probability
) {

}
