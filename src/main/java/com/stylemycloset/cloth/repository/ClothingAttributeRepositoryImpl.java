package com.stylemycloset.cloth.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.stylemycloset.cloth.entity.ClothingAttribute;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.*;

import static com.stylemycloset.cloth.entity.QAttributeOption.attributeOption;
import static com.stylemycloset.cloth.entity.QClothingAttribute.clothingAttribute;


@Repository
@RequiredArgsConstructor
public class ClothingAttributeRepositoryImpl implements ClothingAttributeRepositoryCustom {

    private final JPAQueryFactory factory;

    @Override
    public List<ClothingAttribute> findWithCursorPagination(String keywordLike, Long cursor, int size) {
        //  ID만 페이징으로 조회
        List<Long> ids = factory
                .select(clothingAttribute.id)
                .from(clothingAttribute)
                .where(
                        nameContains(keywordLike),
                        cursorCondition(cursor)
                )
                .orderBy(clothingAttribute.id.asc())
                .limit(size)
                .fetch();

        if (ids.isEmpty()) {
            return List.of();
        }

        // 2단계: in 절로 컬렉션 fetch join 조회
        List<ClothingAttribute> fetched = fetchAttributesWithOptions(ids);

        // fetch join 중복 제거 및 원래 ID 순서 보존
        Map<Long, ClothingAttribute> deduped = new LinkedHashMap<>();
        for (ClothingAttribute attr : fetched) {
            deduped.putIfAbsent(attr.getId(), attr);
        }
        // 원래 ids 순서에 맞춰 정렬 반환
        List<ClothingAttribute> ordered = new ArrayList<>(ids.size());
        for (Long id : ids) {
            ClothingAttribute attr = deduped.get(id);
            if (attr != null) ordered.add(attr);
        }
        return ordered;
    }

    @Override
    public long countByKeyword(String keywordLike) {
        Long count = factory
                .select(clothingAttribute.count())
                .from(clothingAttribute)
                .where(nameContains(keywordLike))
                .fetchOne();
        
        return Optional.ofNullable(count).orElse(0L);
    }




    private BooleanExpression nameContains(String keyword) {
        return StringUtils.hasText(keyword) 
                ? clothingAttribute.name.containsIgnoreCase(keyword.trim()) 
                : null;
    }
    // in 절로 옵션까지 fetch-join하여 속성 목록을 조회
    private List<ClothingAttribute> fetchAttributesWithOptions(List<Long> attributeIds) {
        if (attributeIds == null || attributeIds.isEmpty()) {
            return java.util.List.of();
        }
        return factory
                .selectFrom(clothingAttribute)
                .leftJoin(clothingAttribute.options, attributeOption).fetchJoin()
                .where(clothingAttribute.id.in(attributeIds))
                .orderBy(clothingAttribute.id.asc())
                .fetch();
    }

    private BooleanExpression cursorCondition(Long cursor) {
        return cursor != null ? clothingAttribute.id.gt(cursor) : null;
    }
    

    
}