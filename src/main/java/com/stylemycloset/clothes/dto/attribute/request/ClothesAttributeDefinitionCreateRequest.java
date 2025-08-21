package com.stylemycloset.clothes.dto.attribute.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;


// TODO: 8/16/25 업데이트랑 분리 필요
public record ClothesAttributeDefinitionCreateRequest(
    @NotBlank(message = "속성 이름은 필수입니다.")
    @Size(min = 1, max = 50, message = "속성 이름은 1자 이상 50자 이하여야 합니다.")
    String name,

    @NotNull(message = "선택 가능한 값 목록은 필수입니다.")
    List<String> selectableValues
) {

}