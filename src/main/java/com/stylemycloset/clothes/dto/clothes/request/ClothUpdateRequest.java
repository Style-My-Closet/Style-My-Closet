package com.stylemycloset.clothes.dto.clothes.request;

import com.stylemycloset.clothes.dto.attribute.request.AttributeRequestDto;
import jakarta.validation.constraints.NotNull;
import java.util.List;


public record ClothUpdateRequest(
    @NotNull
    String name,
    @NotNull
    String type,
    List<AttributeRequestDto> attributes
) {

} 