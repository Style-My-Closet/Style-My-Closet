package com.stylemycloset.cloth.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import com.stylemycloset.cloth.dto.AttributeDto;
import com.stylemycloset.cloth.entity.ClothingCategoryType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ClothItemDto {
    private String id;
    private String ownerId;
    private String name;
    private String imageUrl;
    private ClothingCategoryType type;
    private List<AttributeDto> attributes;

    @QueryProjection
    public ClothItemDto(long id, long ownerId, String name, String imageUrl, ClothingCategoryType type) {
        this.id = String.valueOf(id);
        this.ownerId = String.valueOf(ownerId);
        this.name = name;
        this.imageUrl = imageUrl;
        this.type = type;
        this.attributes = List.of();
    }

    @QueryProjection
    public ClothItemDto(long id, long ownerId, String name, String imageUrl, ClothingCategoryType type, List<AttributeDto> attributes) {
        this.id = String.valueOf(id);
        this.ownerId = String.valueOf(ownerId);
        this.name = name;
        this.imageUrl = imageUrl;
        this.type = type;
        this.attributes = attributes != null ? attributes : List.of();
    }


    public void setAttributes(List<AttributeDto> attributes) {
        this.attributes = attributes != null ? attributes : List.of();
    }
}