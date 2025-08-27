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

  @Query("""
      SELECT cad
      FROM ClothesAttributeDefinition cad
      LEFT JOIN FETCH cad.selectableValues
      WHERE cad.id = :definitionId
      """)
  Optional<ClothesAttributeDefinition> findByIdFetchSelectableValues(
      @Param("definitionId") Long definitionId
  );

  @Query("""
      SELECT COUNT(cav) > 0
      FROM ClothesAttributeDefinition cav
      WHERE cav.name = :definitionName
      """)
  boolean existsByAttributeDefinition(@Param("definitionName") String definitionName);

}