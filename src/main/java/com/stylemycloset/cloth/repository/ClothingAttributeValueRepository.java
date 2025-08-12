package com.stylemycloset.cloth.repository;

import com.stylemycloset.cloth.entity.ClothingAttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface ClothingAttributeValueRepository extends JpaRepository<ClothingAttributeValue, Long> {


    List<ClothingAttributeValue> findByAttributeId(Long attributeId);
    
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update ClothingAttributeValue v set v.deletedAt = CURRENT_TIMESTAMP where v.cloth.id = :clothId and v.deletedAt is null")
    void softDeleteAllByClothId(@Param("clothId") Long clothId);
    
}