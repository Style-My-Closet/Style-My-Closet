package com.stylemycloset.ootd.dto;

import com.stylemycloset.clothes.dto.clothes.AttributeDto;
import java.util.List;

public record OotdItemDto(
    Long id,
    String name,
    String imageUrl,
    String type,
    List<AttributeDto> attributes
) {

}