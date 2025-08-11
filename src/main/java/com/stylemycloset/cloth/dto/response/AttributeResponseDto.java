package com.stylemycloset.cloth.dto.response;

import java.util.List;

public record AttributeResponseDto(
        String id,
        String name,
        List<String> selectableValues
) {}


