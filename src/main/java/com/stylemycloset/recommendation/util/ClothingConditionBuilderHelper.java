package com.stylemycloset.recommendation.util;

import com.stylemycloset.clothes.entity.clothes.ClothesAttributeSelectedValue;
import com.stylemycloset.recommendation.entity.ClothingCondition;
import com.stylemycloset.recommendation.entity.Color;
import com.stylemycloset.recommendation.entity.PantsLength;
import com.stylemycloset.recommendation.entity.SleeveLength;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClothingConditionBuilderHelper {

    public ClothingCondition.ClothingConditionBuilder addClothingAttributes(
        ClothingCondition.ClothingConditionBuilder builder,
        List<ClothesAttributeSelectedValue> values
    ) {
        // color 필드 세팅
        values.stream()
            .filter(v -> v.getSelectableValue().getDefinition() != null && v.getSelectableValue().getDefinition().getName().equalsIgnoreCase("COLOR"))
            .findFirst()
            .ifPresent(v -> {
                try {
                    Color color = Color.valueOf(v.getSelectableValue().getValue().toUpperCase());
                    builder.color(color);
                } catch (IllegalArgumentException ignored) {
                    // enum 변환 실패하면 무시
                }
            });

        // sleeveLength 필드 세팅
        values.stream()
            .filter(v -> v.getSelectableValue().getDefinition() != null && v.getSelectableValue().getDefinition().getName().equalsIgnoreCase("SleeveLength"))
            .findFirst()
            .ifPresent(v -> {
                try {
                    SleeveLength sleeve = SleeveLength.valueOf(v.getSelectableValue().getValue().toUpperCase());
                    builder.sleeveLength(sleeve);
                } catch (IllegalArgumentException ignored) {}
            });

        // pantsLength 필드 세팅
        values.stream()
            .filter(v -> v.getSelectableValue().getDefinition() != null && v.getSelectableValue().getDefinition().getName().equalsIgnoreCase("PantsLength"))
            .findFirst()
            .ifPresent(v -> {
                try {
                    PantsLength pants = PantsLength.valueOf(v.getSelectableValue().getValue().toUpperCase());
                    builder.pantsLength(pants);
                } catch (IllegalArgumentException ignored) {}
            });

        return builder;
    }

}
