package com.stylemycloset.clothes.repository.attribute;

import com.stylemycloset.clothes.entity.attribute.ClothesAttributeSelectableValue;
import com.stylemycloset.clothes.entity.clothes.ClothesAttributeSelectedValue;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ClothesAttributeDefinitionSelectedRepository extends
    JpaRepository<ClothesAttributeSelectedValue, Long> {

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("""
      UPDATE ClothesAttributeSelectedValue cadv
      SET cadv.deletedAt = CURRENT_TIMESTAMP
      WHERE cadv.selectableValue IN :selectables
         """)
  void softDeleteBySelectableValues(
      @Param("selectables") Collection<ClothesAttributeSelectableValue> selectables
  );

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("""
      DELETE FROM ClothesAttributeSelectedValue cadv
      WHERE cadv.selectableValue IN :selectables
         """)
  void deleteBySelectableValues(
      @Param("selectables") Collection<ClothesAttributeSelectableValue> selectables
  );

}