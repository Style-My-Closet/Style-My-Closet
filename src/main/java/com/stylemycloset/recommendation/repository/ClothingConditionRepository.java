package com.stylemycloset.recommendation.repository;

import com.stylemycloset.recommendation.entity.ClothingCondition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClothingConditionRepository extends JpaRepository<ClothingCondition, Long> {
}
