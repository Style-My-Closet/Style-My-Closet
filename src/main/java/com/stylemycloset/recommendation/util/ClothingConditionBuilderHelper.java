package com.stylemycloset.recommendation.util;

import com.stylemycloset.cloth.entity.ClothingAttributeValue;
import com.stylemycloset.recommendation.entity.ClothingCondition;
import com.stylemycloset.recommendation.entity.Color;
import com.stylemycloset.recommendation.entity.PantsLength;
import com.stylemycloset.recommendation.entity.SleeveLength;
import java.util.List;

public class ClothingConditionBuilderHelper {
    public static ClothingCondition.ClothingConditionBuilder addClothingAttributes(
        ClothingCondition.ClothingConditionBuilder builder,
        List<ClothingAttributeValue> values
    ) {
        // color 필드 세팅
        values.stream()
            .filter(v -> v.getAttribute() != null && v.getAttribute().getName().equalsIgnoreCase("COLOR"))
            .findFirst()
            .ifPresent(v -> {
                try {
                    Color color = Color.valueOf(v.getOption().getValue().toUpperCase());
                    builder.color(color);
                } catch (IllegalArgumentException ignored) {
                    // enum 변환 실패하면 무시
                }
            });

        // sleeveLength 필드 세팅
        values.stream()
            .filter(v -> v.getAttribute() != null && v.getAttribute().getName().equalsIgnoreCase("SLEEVE"))
            .findFirst()
            .ifPresent(v -> {
                try {
                    SleeveLength sleeve = SleeveLength.valueOf(v.getOption().getValue().toUpperCase());
                    builder.sleeveLength(sleeve);
                } catch (IllegalArgumentException ignored) {}
            });

        // pantsLength 필드 세팅
        values.stream()
            .filter(v -> v.getAttribute() != null && v.getAttribute().getName().equalsIgnoreCase("PANTS"))
            .findFirst()
            .ifPresent(v -> {
                try {
                    PantsLength pants = PantsLength.valueOf(v.getOption().getValue().toUpperCase());
                    builder.pantsLength(pants);
                } catch (IllegalArgumentException ignored) {}
            });

        return builder;
    }

}
