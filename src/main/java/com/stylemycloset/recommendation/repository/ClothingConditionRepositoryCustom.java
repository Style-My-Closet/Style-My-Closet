package com.stylemycloset.recommendation.repository;

import com.stylemycloset.recommendation.entity.ClothingCondition;

public interface ClothingConditionRepositoryCustom {
    ClothingCondition findMostSimilarByVector(float[] inputVector);

    boolean saveIfNotDuplicate(ClothingCondition newCondition);
}

