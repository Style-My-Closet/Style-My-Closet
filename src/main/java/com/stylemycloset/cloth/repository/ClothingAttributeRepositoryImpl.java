package com.stylemycloset.cloth.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.stylemycloset.cloth.dto.ClothesAttributeDto;
import com.stylemycloset.cloth.entity.ClothingAttribute;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.stylemycloset.cloth.entity.QClothingAttribute.clothingAttribute;
import static com.stylemycloset.cloth.entity.QClothingAttributeValue.clothingAttributeValue;
import static com.stylemycloset.cloth.entity.QAttributeOption.attributeOption;

@RequiredArgsConstructor
public class ClothingAttributeRepositoryImpl implements ClothingAttributeRepositoryCustom {

    private final JPAQueryFactory factory;

    @Override
    public List<ClothingAttribute> findWithCursorPagination(String keywordLike, Long cursor, int size) {
        var query = factory
                .selectFrom(clothingAttribute)
                .leftJoin(clothingAttribute.options, attributeOption).fetchJoin();
        
        // 키워드 검색 조건 추가
        if (keywordLike != null && !keywordLike.trim().isEmpty()) {
            query.where(clothingAttribute.name.containsIgnoreCase(keywordLike.trim()));
        }
        
        // 커서 기반 페이징
        if (cursor != null) {
            query.where(clothingAttribute.id.gt(cursor));
        }
        
        return query
                .orderBy(clothingAttribute.id.asc())
                .limit(size)
                .fetch();
    }

    @Override
    public long countByKeyword(String keywordLike) {
        var query = factory
                .select(clothingAttribute.count())
                .from(clothingAttribute);
        
        // 키워드 검색 조건 추가
        if (keywordLike != null && !keywordLike.trim().isEmpty()) {
            query.where(clothingAttribute.name.containsIgnoreCase(keywordLike.trim()));
        }
        
        return Optional.ofNullable(query.fetchOne()).orElse(0L);
    }

    @Override
    public Map<Long, List<String>> findSelectableValuesByAttributeIds(List<Long> attributeIds) {
        if (attributeIds.isEmpty()) {
            return Map.of();
        }
        
        List<SelectableValueResult> results = factory
                .select(Projections.constructor(SelectableValueResult.class,
                        attributeOption.attribute.id,
                        attributeOption.value
                ))
                .from(attributeOption)
                .where(attributeOption.attribute.id.in(attributeIds))
                .fetch();
        
        return results.stream()
                .collect(Collectors.groupingBy(
                        SelectableValueResult::attributeId,
                        Collectors.mapping(
                                SelectableValueResult::value,
                                Collectors.toList()
                        )
                ));
    }

    @Override
    public Map<Long, List<ClothesAttributeDto>> findAttributesByClothIds(List<Long> clothIds) {
        if (clothIds.isEmpty()) {
            return Map.of();
        }
        
        // 1단계: 모든 의류의 attributes와 values를 한 번에 조회
        List<AttributeResult> results = factory
                .select(Projections.constructor(AttributeResult.class,
                        clothingAttributeValue.cloth.id,
                        clothingAttribute.id.stringValue(),
                        clothingAttribute.name,
                        attributeOption.value
                ))
                .from(clothingAttributeValue)
                .leftJoin(clothingAttributeValue.attribute, clothingAttribute)
                .leftJoin(clothingAttributeValue.option, attributeOption)
                .where(clothingAttributeValue.cloth.id.in(clothIds))
                .fetch();
        
        // 2단계: 조회된 속성들의 selectableValues를 한 번에 조회
        List<Long> attributeIds = results.stream()
                .map(result -> Long.valueOf(result.definitionId()))
                .distinct()
                .toList();
        
        Map<Long, List<String>> selectableValuesMap = findSelectableValuesByAttributeIds(attributeIds);
        
        // 3단계: clothId별로 그룹화하여 완전한 DTO 생성
        return results.stream()
                .collect(Collectors.groupingBy(
                        AttributeResult::clothId,
                        Collectors.mapping(
                                result -> new ClothesAttributeDto(
                                        Long.valueOf(result.definitionId()),
                                        result.definitionName(),
                                        selectableValuesMap.getOrDefault(Long.valueOf(result.definitionId()), List.of()),
                                        result.value()
                                ),
                                Collectors.toList()
                        )
                ));
    }
    
    // 내부 클래스들
    private record AttributeResult(
            Long clothId,
            String definitionId,
            String definitionName,
            String value
    ) {}
    
    private record SelectableValueResult(
            Long attributeId,
            String value
    ) {}
} 