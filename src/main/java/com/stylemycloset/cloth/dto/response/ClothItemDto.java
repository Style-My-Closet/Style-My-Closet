package com.stylemycloset.cloth.dto.response;

import com.stylemycloset.cloth.dto.AttributeDto;
import com.stylemycloset.cloth.entity.Cloth;
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

    public ClothItemDto(Cloth cloth) {
        List<AttributeDto> attributest= cloth.getAttributeValues()
                .stream()
                .map(AttributeDto::from)
                .toList();


        this.id = cloth.getId().toString();
        this.ownerId = cloth.getCloset().getUser().getId().toString();
        this.name = cloth.getName();
        this.imageUrl = cloth.getBinaryContent().getImageUrl();
        this.type= cloth.getCategory().getName();
        this.attributes = attributest;
    }
}