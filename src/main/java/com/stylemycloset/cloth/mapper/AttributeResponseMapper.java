package com.stylemycloset.cloth.mapper;

import com.stylemycloset.cloth.dto.response.AttributeResponseDto;
import com.stylemycloset.cloth.entity.ClothingAttribute;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AttributeResponseMapper {
    
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(
        target = "selectableValues",
        expression = "java(entity.getActiveOptions() == null ? java.util.Collections.emptyList() : entity.getActiveOptions().stream().map(option -> option.getValue()).toList())"
    )
    AttributeResponseDto toDto(ClothingAttribute entity);
} 