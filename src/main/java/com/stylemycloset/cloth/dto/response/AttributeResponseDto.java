package com.stylemycloset.cloth.dto.response;

import java.util.List;

public record AttributeResponseDto(
        Long id,
        String name,
        List<String> selectableValues
) {}


