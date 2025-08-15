package com.stylemycloset.cloth.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import com.stylemycloset.cloth.entity.Cloth;
import com.stylemycloset.cloth.entity.ClothingCategoryType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.Objects;

@Getter
@NoArgsConstructor
public class ClothListItemDto {
    private Long id;
    private Long ownerId;
    private String name;
    private String imageUrl;
    private ClothingCategoryType type;

    @QueryProjection
    public ClothListItemDto(Long id, Long ownerId, String name, String imageUrl, ClothingCategoryType type) {
        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        this.imageUrl = imageUrl;
        this.type = type;
    }

    public static ClothListItemDto from(Cloth cloth) {
        Objects.requireNonNull(cloth, "Cloth cannot be null");
        Objects.requireNonNull(cloth.getCloset(), "Closet cannot be null");
        Objects.requireNonNull(cloth.getCloset().getUserId(), "User cannot be null");
        Objects.requireNonNull(cloth.getCategory(), "Category cannot be null");

        return new ClothListItemDto(
            cloth.getId(),
            cloth.getCloset().getUserId(),
            cloth.getName(),
            null,
            cloth.getCategory().getName()
        );
    }


} 