package com.stylemycloset.recommendation.util;

import com.stylemycloset.recommendation.entity.Color;
import com.stylemycloset.recommendation.entity.Length;
import com.stylemycloset.recommendation.entity.Material;
import java.util.Arrays;


public class ClothingVectorizer {

    // 전체 벡터 길이 = 모든 enum 값의 총합
    private static final int COLOR_SIZE = Color.values().length;
    private static final int LENGTH_SIZE = Length.values().length;
    private static final int MATERIAL_SIZE = Material.values().length;
    public static final int VECTOR_SIZE = COLOR_SIZE + LENGTH_SIZE+ MATERIAL_SIZE;


    public static float[] vectorize(Color color, Length length, Material material) {
        float[] vector = new float[VECTOR_SIZE];
        Arrays.fill(vector, 0f);

        if (color != null) {
            vector[color.ordinal()] = 1f;
        }

        if (length != null) {
            vector[COLOR_SIZE + length.ordinal()] = 1f;
        }

        if  (material != null) {
            vector[COLOR_SIZE + LENGTH_SIZE + material.ordinal()] = 1f;
        }

        return vector;
    }



}
