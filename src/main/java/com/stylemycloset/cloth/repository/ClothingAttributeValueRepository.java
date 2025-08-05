package com.stylemycloset.cloth.repository;

import com.stylemycloset.cloth.entity.ClothingAttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClothingAttributeValueRepository extends JpaRepository<ClothingAttributeValue, Long> {

    void deleteByAttributeId(Long attributeId);
    
    List<ClothingAttributeValue> findByAttributeId(Long attributeId);
} 