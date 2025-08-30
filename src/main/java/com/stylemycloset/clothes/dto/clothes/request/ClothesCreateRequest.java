package com.stylemycloset.clothes.dto.clothes.request;

import com.stylemycloset.clothes.dto.attribute.request.AttributeRequestDto;
import java.util.List;

public record ClothesCreateRequest(
    Long ownerId,
    String name,
    String type,
    List<AttributeRequestDto> attributes
) {



}



