package com.stylemycloset.cloth.dto.response;

import com.stylemycloset.cloth.entity.Cloth;
import com.stylemycloset.cloth.entity.ClothingCategoryType;

public record ClothListItemDto(
    String id,
    String ownerId,
    String name,
    String imageUrl,
    ClothingCategoryType type
) {
    public static ClothListItemDto from(Cloth cloth) {
        return new ClothListItemDto(
            cloth.getId().toString(),
            cloth.getCloset().getUser().getId().toString(),
            cloth.getName(),
            cloth.getBinaryContent() != null ? cloth.getBinaryContent().getImageUrl() : null,
            cloth.getCategory().getName()
        );
    }
} 