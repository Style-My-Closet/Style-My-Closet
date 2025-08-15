package com.stylemycloset.cloth.dto;

import com.stylemycloset.cloth.entity.ClothingCategoryType;
import com.stylemycloset.cloth.entity.Cloth;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
public class ClothResponseDto {
    private Long id;
    private Long ownerId;
    private String name;
    private String imageUrl;
    private ClothingCategoryType type;
    private List<AttributeDto> attributes;
    private String productUrl; // 추출 API 응답을 위한 원본 상품 URL

    @QueryProjection
    public ClothResponseDto(Long id, Long ownerId, String name, String imageUrl, ClothingCategoryType type) {
        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        this.imageUrl = imageUrl;
        this.type = type;
        this.attributes = List.of(); // 속성은 별도 쿼리로 처리
    }

    public ClothResponseDto(Cloth cloth) {
        Objects.requireNonNull(cloth, "Cloth cannot be null");
        Objects.requireNonNull(cloth.getCloset(), "Closet cannot be null");
        Objects.requireNonNull(cloth.getCloset().getUserId(), "User cannot be null");
        Objects.requireNonNull(cloth.getCategory(), "Category cannot be null");

        this.id = cloth.getId();
        this.ownerId = cloth.getCloset().getUserId();
        this.name = cloth.getName();
        this.imageUrl = null;
        this.type = cloth.getCategory().getName();

        this.attributes = cloth.getAttributeValues()
                .stream()
                .map(AttributeDto::from)
                .toList();
    }
} 