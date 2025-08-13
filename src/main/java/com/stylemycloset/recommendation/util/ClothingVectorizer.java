package com.stylemycloset.recommendation.util;

import com.stylemycloset.recommendation.entity.Color;
import com.stylemycloset.recommendation.entity.PantsLength;
import com.stylemycloset.recommendation.entity.SleeveLength;
import java.util.Arrays;


public class ClothingVectorizer {

    // 전체 벡터 길이 = 모든 enum 값의 총합
    private static final int COLOR_SIZE = Color.values().length;
    private static final int SLEEVE_SIZE = SleeveLength.values().length;
    private static final int PANTS_SIZE = PantsLength.values().length;
    public static final int VECTOR_SIZE = COLOR_SIZE + SLEEVE_SIZE + PANTS_SIZE;


    public static float[] vectorize(Color color, SleeveLength sleeveLength, PantsLength pantsLength) {
        float[] vector = new float[VECTOR_SIZE];
        Arrays.fill(vector, 0f);

        if (color != null) {
            vector[color.ordinal()] = 1f;
        }

        if (sleeveLength != null) {
            vector[COLOR_SIZE + sleeveLength.ordinal()] = 1f;
        }

        if (pantsLength != null) {
            vector[COLOR_SIZE + SLEEVE_SIZE + pantsLength.ordinal()] = 1f;
        }

        return vector;
    }



}
