package com.stylemycloset.ootd.dto;

import java.util.List;

public record ClothesAttributeWithDefDto(
    Long definitionId,
    String definitionName,
    List<String> selectableValues,
    String value
) {

}