package com.stylemycloset.cloth.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.stylemycloset.cloth.dto.AttributeDto;
import com.stylemycloset.cloth.dto.ClothResponseDto;
import com.stylemycloset.cloth.dto.QAttributeDto;
import com.stylemycloset.cloth.dto.QClothResponseDto;
import com.stylemycloset.cloth.dto.response.ClothItemDto;
import com.stylemycloset.cloth.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.stylemycloset.binarycontent.entity.QBinaryContent.binaryContent;
import static com.stylemycloset.cloth.entity.QCloset.closet;
import static com.stylemycloset.cloth.entity.QCloth.cloth;
import static com.stylemycloset.cloth.entity.QClothingAttribute.clothingAttribute;
import static com.stylemycloset.cloth.entity.QClothingAttributeValue.clothingAttributeValue;
import static com.stylemycloset.cloth.entity.QClothingCategory.clothingCategory;


@Repository
@RequiredArgsConstructor
public class ClothRepositoryImpl implements ClothRepositoryCustom {

    private final JPAQueryFactory factory;
    private static final QAttributeOption selectedOption = new QAttributeOption("selectedOption");


    
    @Override
    public long countByUserId(Long userId) {
        Long count = factory
                .select(cloth.count())
                .from(cloth)
                .where(userIdEq(userId))
                .fetchOne();

        return Optional.ofNullable(count).orElse(0L);
    }

    @Override
    public List<ClothItemDto> findClothItemDtosWithCursorPagination(Long userId, Long idAfter, int limitPlusOne, boolean isDescending, boolean hasIdAfter) {
        // 1) ID만 페이징으로 먼저 조회
        List<Long> ids = fetchPagedIds(userId, idAfter, limitPlusOne, isDescending, hasIdAfter);
        if (ids.isEmpty()) {
            return List.of();
        }

        // 2) 기본 정보 조회 (ownerId, name, imageUrl, category)
        Map<Long, ClothItemDto> idToItem = fetchBaseInfo(ids);

    // 3) 속성 조회 후 매핑 (IN으로 한 번에)
        List<Tuple> attrRows = fetchAttributeTuples(ids);
        Map<Long, List<AttributeDto>> attrsByClothId = mapAttributesByClothId(attrRows);
        Set<String> attributeIdsInPage = collectAttributeIds(attrsByClothId);
        Map<String, List<String>> selectableValuesByAttributeId = fetchSelectableValues(attributeIdsInPage);
        enrichAttributes(attrsByClothId, selectableValuesByAttributeId);

        // 4) 원래 페이징 순서를 보존하며 attributes 설정
        List<ClothItemDto> result = new ArrayList<>(ids.size());
        for (Long cId : ids) {
            ClothItemDto dto = idToItem.get(cId);
            if (dto != null) {
                dto.setAttributes(attrsByClothId.getOrDefault(cId, java.util.List.of()));
                result.add(dto);
            }
        }
        return result;
    }

    private List<Long> fetchPagedIds(Long userId, Long idAfter, int limitPlusOne, boolean isDescending, boolean hasIdAfter) {
        JPAQuery<Long> idQuery = factory
                .select(cloth.id)
                .from(cloth)
                .leftJoin(cloth.closet, closet)
                .where(userIdEq(userId));

        if (hasIdAfter) {
            idQuery.where(cursorCondition(idAfter, isDescending));
        }
        idQuery.orderBy(isDescending ? cloth.id.desc() : cloth.id.asc());
        return idQuery.limit(limitPlusOne).fetch();
    }

    private Map<Long, ClothItemDto> fetchBaseInfo(List<Long> ids) {
        List<Tuple> baseRows = factory
                .select(
                        cloth.id,
                        closet.userId,
                        cloth.name,
                        binaryContent.imageUrl,
                        clothingCategory.name
                )
                .from(cloth)
                .leftJoin(cloth.closet, closet)
                .leftJoin(binaryContent).on(binaryContent.id.eq(cloth.binaryContentId))
                .leftJoin(cloth.category, clothingCategory)
                .where(cloth.id.in(ids))
                .fetch();

        Map<Long, ClothItemDto> idToItem = new HashMap<>();
        for (Tuple row : baseRows) {
            Long cId = row.get(cloth.id);
            Long ownerId = row.get(closet.userId);
            String nameVal = row.get(cloth.name);
            String imageUrlVal = row.get(binaryContent.imageUrl);
            ClothingCategoryType typeVal = row.get(clothingCategory.name);
            ClothItemDto dto = new ClothItemDto(cId, ownerId, nameVal, imageUrlVal, typeVal);
            idToItem.put(cId, dto);
        }
        return idToItem;
    }

    private List<Tuple> fetchAttributeTuples(List<Long> ids) {
        return factory
                .select(
                        clothingAttributeValue.cloth.id,
                        new QAttributeDto(
                                clothingAttribute.id,
                                clothingAttribute.name,
                                selectedOption.value
                        )
                )
                .from(clothingAttributeValue)
                .leftJoin(clothingAttributeValue.attribute, clothingAttribute)
                .leftJoin(clothingAttributeValue.option, selectedOption)
                .where(
                        clothingAttributeValue.cloth.id.in(ids),
                        clothingAttributeValue.deletedAt.isNull(),
                        selectedOption.deletedAt.isNull()
                )
                .fetch();
    }

    private Map<Long, List<AttributeDto>> mapAttributesByClothId(List<Tuple> attrRows) {
        Map<Long, List<AttributeDto>> attrsByClothId = new HashMap<>();
        for (Tuple row : attrRows) {
            Long cId = row.get(clothingAttributeValue.cloth.id);
            AttributeDto attr = row.get(1, AttributeDto.class);
            attrsByClothId.computeIfAbsent(cId, k -> new ArrayList<>()).add(attr);
        }
        return attrsByClothId;
    }

    private Set<String> collectAttributeIds(Map<Long, List<AttributeDto>> attrsByClothId) {
        Set<String> attributeIdsInPage = new HashSet<>();
        for (List<AttributeDto> list : attrsByClothId.values()) {
            for (AttributeDto attr : list) {
                if (attr != null && attr.definitionId() != null) {
                    attributeIdsInPage.add(attr.definitionId());
                }
            }
        }
        return attributeIdsInPage;
    }

    private Map<String, List<String>> fetchSelectableValues(Set<String> attributeIdsInPage) {
        Map<String, List<String>> selectableValuesByAttributeId = new HashMap<>();
        if (attributeIdsInPage.isEmpty()) {
            return selectableValuesByAttributeId;
        }

        List<Long> attrIdLongs = attributeIdsInPage.stream()
                .map(idStr -> {
                    try { return java.lang.Long.parseLong(idStr); } catch (NumberFormatException e) { return null; }
                })
                .filter(java.util.Objects::nonNull)
                .toList();

        if (attrIdLongs.isEmpty()) {
            return selectableValuesByAttributeId;
        }

        QAttributeOption allOptions = new QAttributeOption("allOptions");
        List<Tuple> optionRows = factory
                .select(clothingAttribute.id, allOptions.value)
                .from(allOptions)
                .leftJoin(allOptions.attribute, clothingAttribute)
                .where(clothingAttribute.id.in(attrIdLongs))
                .fetch();

        for (Tuple t : optionRows) {
            Long attrId = t.get(clothingAttribute.id);
            String optVal = t.get(allOptions.value);
            if (attrId != null && optVal != null) {
                String key = attrId.toString();
                selectableValuesByAttributeId
                        .computeIfAbsent(key, k -> new java.util.ArrayList<>())
                        .add(optVal);
            }
        }
        return selectableValuesByAttributeId;
    }

    private void enrichAttributes(Map<Long, List<AttributeDto>> attrsByClothId,
                                  Map<String, List<String>> selectableValuesByAttributeId) {
        for (Map.Entry<Long, List<AttributeDto>> entry : attrsByClothId.entrySet()) {
            List<AttributeDto> enriched = new ArrayList<>();
            for (AttributeDto a : entry.getValue()) {
                List<String> selectable = selectableValuesByAttributeId.getOrDefault(a.definitionId(), List.of());
                enriched.add(new AttributeDto(a.definitionId(), a.definitionName(), selectable, a.value()));
            }
            entry.setValue(enriched);
        }
    }

    @Override
    public Optional<ClothResponseDto> findClothResponseDtoById(Long clothId) {
        ClothResponseDto result = factory
                .select(new QClothResponseDto(
                    cloth.id,
                    closet.userId,
                    cloth.name,
                    binaryContent.imageUrl,
                    clothingCategory.name
                ))
                .from(cloth)
                .leftJoin(cloth.closet, closet)
                .leftJoin(binaryContent).on(binaryContent.id.eq(cloth.binaryContentId))
                .leftJoin(cloth.category, clothingCategory)
                .where(clothIdEq(clothId))
                .fetchOne();

        return Optional.ofNullable(result);
    }

    public List<AttributeDto> findAttributeDtosByClothId(Long clothId) {
        return factory
                .select(
                    new QAttributeDto(
                    clothingAttribute.id,
                    clothingAttribute.name,
                    selectedOption.value
                ))
                .from(clothingAttributeValue)
                .leftJoin(clothingAttributeValue.attribute, clothingAttribute)
                .leftJoin(clothingAttributeValue.option, selectedOption)
                .where(
                    clothingAttributeValue.cloth.id.eq(clothId),
                    clothingAttributeValue.deletedAt.isNull(),
                    selectedOption.deletedAt.isNull()
                )
                .fetch();
    }
    

    private BooleanExpression clothIdEq(Long clothId) {
        return clothId != null ? cloth.id.eq(clothId) : null;
    }
    

    private BooleanExpression userIdEq(Long userId) {
        return userId != null ? closet.userId.eq(userId) : null;
    }
    

    private BooleanExpression cursorCondition(Long idAfter, boolean isDescending) {
        if (idAfter == null) return null;
        return isDescending ? cloth.id.lt(idAfter) : cloth.id.gt(idAfter);
    }
    




}