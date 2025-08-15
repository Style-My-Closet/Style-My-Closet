package com.stylemycloset.cloth.dto.response;

import com.stylemycloset.cloth.entity.ClothingCategoryType;
import com.stylemycloset.cloth.dto.AttributeDto;
import com.stylemycloset.cloth.entity.Cloth;

import java.util.List;
import java.util.Objects;

public record ClothUpdateResponseDto(
        Long id,
        Long ownerId,
        String name,
        String imageUrl,
        ClothingCategoryType category,
        List<AttributeDto> attributes

) {
    public static ClothUpdateResponseDto from(Cloth cloth) {
       Objects.requireNonNull(cloth, "Cloth cannot be null");
       Objects.requireNonNull(cloth.getCloset(), "Closet cannot be null");
       Objects.requireNonNull(cloth.getCloset().getUserId(), "User cannot be null");
       Objects.requireNonNull(cloth.getCategory(), "Category cannot be null");

       List<AttributeDto> attributest= cloth.getAttributeValues()
                .stream()
                .map(AttributeDto::from)
                .toList();
        return new ClothUpdateResponseDto(
                cloth.getId(),
                cloth.getCloset().getUserId(),
                cloth.getName(),
                null,
                cloth.getCategory().getName(),
                attributest
        );
    }
}
