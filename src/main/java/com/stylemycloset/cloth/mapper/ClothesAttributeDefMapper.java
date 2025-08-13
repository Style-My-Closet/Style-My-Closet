package com.stylemycloset.cloth.mapper;

import com.stylemycloset.cloth.dto.ClothesAttributeDefDto;
import com.stylemycloset.cloth.entity.ClothingAttribute;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ClothesAttributeDefMapper {
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "selectableValues", expression = "java(entity.getActiveOptions().stream().map(option -> option.getValue()).toList())")
    ClothesAttributeDefDto toDto(ClothingAttribute entity);
    List<ClothesAttributeDefDto> toDtoList(List<ClothingAttribute> entities);
}