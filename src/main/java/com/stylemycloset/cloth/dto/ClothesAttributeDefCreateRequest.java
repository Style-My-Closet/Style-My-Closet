package com.stylemycloset.cloth.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.stylemycloset.common.util.FlexibleStringListDeserializer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;


public record ClothesAttributeDefCreateRequest(
    @NotBlank(message = "속성 이름은 필수입니다.")
    @Size(min = 1, max = 50, message = "속성 이름은 1자 이상 50자 이하여야 합니다.")
    @JsonAlias({"label", "title"})
    String name,                    // 속성 정의 이름
    
    @NotNull(message = "선택 가능한 값 목록은 필수입니다.")
    @JsonAlias({"options", "values", "selectable_values"})
    @JsonDeserialize(using = FlexibleStringListDeserializer.class)
    List<String> selectableValues   // 선택 가능한 값 목록
) {
} 