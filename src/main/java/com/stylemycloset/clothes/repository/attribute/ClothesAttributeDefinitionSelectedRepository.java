package com.stylemycloset.clothes.repository.attribute;

import com.stylemycloset.clothes.entity.clothes.ClothesAttributeSelectedValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClothesAttributeDefinitionSelectedRepository extends
    JpaRepository<ClothesAttributeSelectedValue, Long> {

}