package com.stylemycloset.cloth.dto;

import com.stylemycloset.cloth.entity.ClothingCategoryType;
import com.stylemycloset.cloth.entity.Cloth;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ClothResponseDto {
    private String id;  // UUID 형태로 변경
    private String ownerId;  // UUID 형태로 변경
    private String name;
    private String imageUrl;
    private ClothingCategoryType type;
    private List<AttributeDto> attributes;  // attributeList -> attributes로 변경
    private String productUrl; // 추출 API 응답을 위한 원본 상품 URL

    @QueryProjection
    public ClothResponseDto(Long id, Long ownerId, String name, String imageUrl, ClothingCategoryType type) {
        this.id = id.toString();
        this.ownerId = ownerId.toString();
        this.name = name;
        this.imageUrl = imageUrl;
        this.type = type;
        this.attributes = List.of(); // 속성은 별도 쿼리로 처리
    }

    public ClothResponseDto(Cloth cloth) {
        this.id = cloth.getId().toString();  // Long을 String으로 변환
        this.ownerId = cloth.getCloset().getUser().getId().toString();  // Long을 String으로 변환
        this.name = cloth.getName();
        this.imageUrl = cloth.getBinaryContent() != null ? cloth.getBinaryContent().getImageUrl() : null;
        this.type = cloth.getCategory().getName();

        this.attributes = cloth.getAttributeValues()  // attributeList -> attributes로 변경
                .stream()
                .map(AttributeDto::from)
                .toList();
    }
} 