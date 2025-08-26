package com.stylemycloset.clothes.repository.attribute;

import com.stylemycloset.clothes.entity.attribute.ClothesAttributeDefinition;
import com.stylemycloset.clothes.repository.attribute.impl.ClothesAttributeDefinitionRepositoryCustom;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ClothesAttributeDefinitionRepository extends
    JpaRepository<ClothesAttributeDefinition, Long>,
    ClothesAttributeDefinitionRepositoryCustom {

  Optional<ClothesAttributeDefinition> findByIdAndDeletedAtIsNull(Long definitionId);

  @Query("""
      SELECT COUNT(cav) > 0
      FROM ClothesAttributeDefinition cav
      WHERE cav.name = :definitionName
      AND cav.deletedAt IS NOT NULL
      """)
  boolean existsByActiveAttributeDefinition(@Param("definitionName") String definitionName);

}