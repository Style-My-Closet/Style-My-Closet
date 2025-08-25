package com.stylemycloset.recommendation.repository;

import com.stylemycloset.recommendation.entity.ClothingCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClothingConditionRepository extends JpaRepository<ClothingCondition, Long> {

    @Query(value = """
        SELECT *
        FROM clothing_conditions
        ORDER BY embedding <=> :inputVector
        LIMIT 1
        """, nativeQuery = true)
    ClothingCondition findMostSimilar(@Param("inputVector") float[] inputVector);
}
