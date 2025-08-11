package com.stylemycloset.cloth.repository;

import com.stylemycloset.cloth.entity.ClothingAttribute;

import java.util.List;
import java.util.Map;

public interface ClothingAttributeRepositoryCustom {
    List<ClothingAttribute> findWithCursorPagination(String keywordLike, Long cursor, int size);
    long countByKeyword(String keywordLike);

}