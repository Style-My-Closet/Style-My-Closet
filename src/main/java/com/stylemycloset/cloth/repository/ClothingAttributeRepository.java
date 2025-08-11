package com.stylemycloset.cloth.repository;

import com.stylemycloset.cloth.entity.ClothingAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface ClothingAttributeRepository extends JpaRepository<ClothingAttribute, Long>, ClothingAttributeRepositoryCustom {
    
    Optional<ClothingAttribute> findByName(String name);


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update AttributeOption o set o.deleteAt = CURRENT_TIMESTAMP " +
           "where o.attribute.id = :attributeId and o.deleteAt is null " +
           "and o.value not in :targetValues")
    void softDeleteMissingOptions(@Param("attributeId") Long attributeId,
                                  @Param("targetValues") java.util.Collection<String> targetValues);
} 