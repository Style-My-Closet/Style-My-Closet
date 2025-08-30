package com.stylemycloset.recommendation.dto;

public record ConditionWithDistance(
    Long id,
    float[] embedding,
    Double cosineDistance
) {}
