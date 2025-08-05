package com.stylemycloset.cloth.repository;

import com.stylemycloset.cloth.entity.AttributeOption;
import com.stylemycloset.cloth.entity.ClothingAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AttributeOptionRepository extends JpaRepository<AttributeOption, Long> {
    Optional<AttributeOption> findByAttributeAndValue(ClothingAttribute attribute, String value);
} 