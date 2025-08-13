package com.stylemycloset.cloth.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import com.stylemycloset.cloth.entity.Cloth;
import com.stylemycloset.cloth.entity.ClothingCategoryType;
import java.util.Objects;

public record ClothListItemDto(
    String id,
    String ownerId,
    String name,
    String imageUrl,
    ClothingCategoryType type
) {
    @QueryProjection
    public ClothListItemDto(Long id, Long ownerId, String name, String imageUrl, ClothingCategoryType type) {
        this(
            id.toString(),
            ownerId.toString(),
            name,
            imageUrl,
            type
        );
    }

    public static ClothListItemDto from(Cloth cloth) {
        Objects.requireNonNull(cloth, "Cloth cannot be null");
        Objects.requireNonNull(cloth.getCloset(), "Closet cannot be null");
        Objects.requireNonNull(cloth.getCloset().getUserId(), "User cannot be null");
        Objects.requireNonNull(cloth.getCategory(), "Category cannot be null");

        return new ClothListItemDto(
            cloth.getId().toString(),
            cloth.getCloset().getUserId().toString(),
            cloth.getName(),
            null,
            cloth.getCategory().getName()
        );
    }


} 