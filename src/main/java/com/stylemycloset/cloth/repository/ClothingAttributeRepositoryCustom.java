package com.stylemycloset.cloth.repository;

import com.stylemycloset.cloth.dto.ClothesAttributeDto;
import com.stylemycloset.cloth.entity.ClothingAttribute;

import java.util.List;
import java.util.Map;

public interface ClothingAttributeRepositoryCustom {
    List<ClothingAttribute> findWithCursorPagination(String keywordLike, Long cursor, int size);
    long countByKeyword(String keywordLike);
    

    Map<Long, List<String>> findSelectableValuesByAttributeIds(List<Long> attributeIds);

    Map<Long, List<ClothesAttributeDto>> findAttributesByClothIds(List<Long> clothIds);
}