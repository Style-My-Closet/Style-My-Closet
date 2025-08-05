package com.stylemycloset.cloth.dto;

import com.stylemycloset.cloth.entity.ClothingCategoryType;
import com.stylemycloset.cloth.entity.Cloth;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ClothResponseDto {
    private Long clothId;
    private Long ownerId;
    private String name;
    private String imageUrl;
    private ClothingCategoryType type;
    private List<AttributeDto> attributeList;

    public ClothResponseDto(Cloth cloth) {
        this.clothId = cloth.getId();
        this.ownerId = cloth.getCloset().getUser().getId();
        this.name = cloth.getName();
        this.imageUrl = cloth.getBinaryContent() != null ? cloth.getBinaryContent().getImageUrl() : null;
        this.type = cloth.getCategory().getName();

        this.attributeList = cloth.getAttributeValues()
                .stream()
                .map(AttributeDto::from)
                .toList();
    }
} 