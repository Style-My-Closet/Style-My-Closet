package com.stylemycloset.clothes.repository.attribute;

import com.stylemycloset.clothes.entity.attribute.ClothesAttributeSelectableValue;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClothesAttributeDefinitionSelectableRepository extends
    JpaRepository<ClothesAttributeSelectableValue, Long> {

  Optional<ClothesAttributeSelectableValue> findByDefinitionIdAndValue(
      Long definitionId,
      String value
  );

} 