package com.stylemycloset.cloth.dto;

import java.util.List;

public record CreateAttributeDto(
        String name,
        List<String> selectableValues // 옵션들 리스트
) {

}
