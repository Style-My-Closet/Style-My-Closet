package com.stylemycloset.cloth.dto.response;

import com.stylemycloset.cloth.entity.ClothingCategoryType;
import com.stylemycloset.cloth.dto.AttributeDto;
import com.stylemycloset.cloth.entity.Cloth;

import java.util.List;

public record ClothUpdateResponseDto(
        String id,
        String ownerId,
        String name,
        String imageUrl,
        ClothingCategoryType category,
        List<AttributeDto> attributes

) {
    public static ClothUpdateResponseDto from(Cloth cloth) {

       List<AttributeDto> attributest= cloth.getAttributeValues()
                .stream()
                .map(AttributeDto::from)
                .toList();
        return new ClothUpdateResponseDto(
                cloth.getId().toString(),
                cloth.getCloset().getUser().getId().toString(),
                cloth.getName(),
                cloth.getBinaryContent() != null ? cloth.getBinaryContent().getImageUrl() : null,
                cloth.getCategory().getName(),
                attributest
        );
    }
}
