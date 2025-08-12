package com.stylemycloset.recommendation.repository;

import com.stylemycloset.recommendation.entity.ClothingFeature;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClothingFeatureRepository extends JpaRepository<ClothingFeature, Long> {
}
