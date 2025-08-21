package com.stylemycloset.ootd.mapper;

import com.stylemycloset.cloth.entity.AttributeOption;
import com.stylemycloset.cloth.entity.Cloth;
import com.stylemycloset.cloth.entity.ClothingAttribute;
import com.stylemycloset.ootd.dto.ClothesAttributeWithDefDto;
import com.stylemycloset.ootd.dto.OotdItemDto;
import com.stylemycloset.ootd.tempEnum.ClothesType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Cloth 엔티티를 OotdItemDto로 변환하는 매퍼
 * 
 * 책임:
 * - Cloth 엔티티의 복잡한 매핑 로직 처리
 * - 속성값과 선택지 정보 변환
 * - 카테고리 타입 변환
 */
@Component
public class OotdItemMapper {

    /**
     * Cloth 엔티티를 OotdItemDto로 변환
     * 
     * @param cloth 변환할 Cloth 엔티티
     * @return OotdItemDto
     */
    public OotdItemDto toDto(Cloth cloth) {
        if (cloth == null) {
            return null;
        }

        List<ClothesAttributeWithDefDto> attributes = mapAttributes(cloth);
        
        return new OotdItemDto(
            cloth.getId(),
            cloth.getName(),
            null, // TODO: 이미지 URL 로직 구현 필요
            mapClothesType(cloth),
            attributes
        );
    }

    /**
     * Cloth 리스트를 OotdItemDto 리스트로 변환
     * 
     * @param clothes 변환할 Cloth 엔티티 리스트
     * @return OotdItemDto 리스트
     */
    public List<OotdItemDto> toDtoList(List<Cloth> clothes) {
        if (clothes == null) {
            return List.of();
        }

        return clothes.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    /**
     * Cloth의 속성값들을 ClothesAttributeWithDefDto로 변환
     * 
     * @param cloth 변환할 Cloth 엔티티
     * @return ClothesAttributeWithDefDto 리스트
     */
    private List<ClothesAttributeWithDefDto> mapAttributes(Cloth cloth) {
        return cloth.getAttributeValues().stream()
            .map(attributeValue -> {
                ClothingAttribute definition = attributeValue.getAttribute(); // 속성의 정의

                // 해당 속성이 가질 수 있는 모든 선택지
                List<String> selectableValues = definition.getOptions().stream()
                    .map(AttributeOption::getValue)
                    .collect(Collectors.toList());

                // 이 옷이 선택한 특정 값을 가져옴
                String chosenValue = attributeValue.getOption().getValue();

                return new ClothesAttributeWithDefDto(
                    definition.getId(),
                    definition.getName(),
                    selectableValues,
                    chosenValue
                );
            })
            .collect(Collectors.toList());
    }

    /**
     * Cloth의 카테고리를 ClothesType enum으로 변환
     * 
     * @param cloth 변환할 Cloth 엔티티
     * @return ClothesType enum 값
     */
    private ClothesType mapClothesType(Cloth cloth) {
        return ClothesType.valueOf(cloth.getCategory().getName().name());
    }
}
