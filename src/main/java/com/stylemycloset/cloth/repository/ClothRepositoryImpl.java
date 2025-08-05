package com.stylemycloset.cloth.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.stylemycloset.cloth.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.stylemycloset.binarycontent.QBinaryContent.binaryContent;
import static com.stylemycloset.cloth.entity.QCloset.closet;
import static com.stylemycloset.cloth.entity.QCloth.cloth;
import static com.stylemycloset.cloth.entity.QClothingAttribute.clothingAttribute;
import static com.stylemycloset.cloth.entity.QClothingAttributeValue.clothingAttributeValue;
import static com.stylemycloset.cloth.entity.QClothingCategory.clothingCategory;

@Repository
@RequiredArgsConstructor
public class ClothRepositoryImpl implements ClothRepositoryCustom {

    private final JPAQueryFactory factory;


    
    @Override
    public Optional<Cloth> findByIdWithAttributes(Long clothId) {
        QAttributeOption selectedOption = new QAttributeOption("selectedOption");
        
        Cloth result = factory
                .selectFrom(cloth)
                .leftJoin(cloth.attributeValues, clothingAttributeValue).fetchJoin()
                .leftJoin(clothingAttributeValue.attribute, clothingAttribute).fetchJoin()
                .leftJoin(clothingAttributeValue.option, selectedOption).fetchJoin()
                .leftJoin(cloth.category, clothingCategory).fetchJoin()
                .leftJoin(cloth.closet, closet).fetchJoin()
                .leftJoin(cloth.binaryContent, binaryContent).fetchJoin()
                .where(cloth.id.eq(clothId))
                .fetchOne();
        
        return Optional.ofNullable(result);
    }

    @Override
    public List<Cloth> findWithCursorPagination(Long userId, Long idAfter, int limitPlusOne, 
                                               boolean isDescending, boolean hasIdAfter) {
        QAttributeOption selectedOption = new QAttributeOption("selectedOption");
        
        var query = factory
                .selectFrom(cloth)
                .leftJoin(cloth.attributeValues, clothingAttributeValue).fetchJoin()
                .leftJoin(clothingAttributeValue.attribute, clothingAttribute).fetchJoin()
                .leftJoin(clothingAttributeValue.option, selectedOption).fetchJoin()
                .leftJoin(cloth.category, clothingCategory).fetchJoin()
                .leftJoin(cloth.closet, closet).fetchJoin()
                .leftJoin(cloth.binaryContent, binaryContent).fetchJoin()
                .where(cloth.closet.user.id.eq(userId));

        if (hasIdAfter) {
            if (isDescending) {
                query.where(cloth.id.lt(idAfter));
            } else {
                query.where(cloth.id.gt(idAfter));
            }
        }
        
        if (isDescending) {
            query.orderBy(cloth.id.desc());
        } else {
            query.orderBy(cloth.id.asc());
        }
        
        return query.limit(limitPlusOne).fetch();
    }
    
    @Override
    public long countByUserId(Long userId) {
        var query = factory
                .select(cloth.count())
                .from(cloth)
                .where(cloth.closet.user.id.eq(userId))
                .fetchOne();

        return Optional.ofNullable(query).orElse(0L);
    }
} 