package com.stylemycloset.cloth.repository;

import com.stylemycloset.cloth.entity.ClothingAttributeValue;
import com.stylemycloset.cloth.entity.AttributeOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface ClothingAttributeValueRepository extends JpaRepository<ClothingAttributeValue, Long> {


    List<ClothingAttributeValue> findByAttributeId(Long attributeId);
    
    // 단방향 관계로 인해 추가된 메서드들
    List<ClothingAttributeValue> findByOption(AttributeOption option);
    
    @Query("SELECT cav FROM ClothingAttributeValue cav WHERE cav.option.id = :optionId AND cav.deletedAt IS NULL")
    List<ClothingAttributeValue> findByOptionId(@Param("optionId") Long optionId);
    
    // 성능 최적화: 배치 조회로 N+1 문제 해결
    @Query("SELECT cav FROM ClothingAttributeValue cav WHERE cav.option.id IN :optionIds AND cav.deletedAt IS NULL")
    List<ClothingAttributeValue> findByOptionIds(@Param("optionIds") List<Long> optionIds);
    
    // 성능 최적화: JOIN FETCH로 관련 데이터 한번에 조회
    @Query("SELECT cav FROM ClothingAttributeValue cav JOIN FETCH cav.cloth JOIN FETCH cav.attribute WHERE cav.option.id = :optionId AND cav.deletedAt IS NULL")
    List<ClothingAttributeValue> findByOptionIdWithDetails(@Param("optionId") Long optionId);
    
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE ClothingAttributeValue cav SET cav.deletedAt = CURRENT_TIMESTAMP WHERE cav.option.id = :optionId AND cav.deletedAt IS NULL")
    void softDeleteAllByOptionId(@Param("optionId") Long optionId);
    
    // 성능 최적화: 여러 옵션의 AttributeValue들을 한번에 삭제
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE ClothingAttributeValue cav SET cav.deletedAt = CURRENT_TIMESTAMP WHERE cav.option.id IN :optionIds AND cav.deletedAt IS NULL")
    void deleteAllByOptionIds(@Param("optionIds") List<Long> optionIds);
    
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update ClothingAttributeValue v set v.deletedAt = CURRENT_TIMESTAMP where v.cloth.id = :clothId and v.deletedAt is null")
    void softDeleteAllByClothId(@Param("clothId") Long clothId);
    
}