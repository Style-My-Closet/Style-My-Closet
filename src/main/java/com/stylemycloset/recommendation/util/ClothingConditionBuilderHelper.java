package com.stylemycloset.recommendation.util;

import com.stylemycloset.clothes.entity.clothes.ClothesAttributeSelectedValue;
import com.stylemycloset.recommendation.entity.ClothingCondition;
import com.stylemycloset.recommendation.entity.Color;
import com.stylemycloset.recommendation.entity.Length;
import com.stylemycloset.recommendation.entity.Material;
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

        // Length 필드 세팅
        values.stream()
            .filter(v -> v.getSelectableValue().getDefinition() != null && v.getSelectableValue().getDefinition().getName().equalsIgnoreCase("Length"))
            .findFirst()
            .ifPresent(v -> {
                try {
                    Length length = Length.valueOf(v.getSelectableValue().getValue().toUpperCase());
                    builder.length(length);
                } catch (IllegalArgumentException ignored) {}
            });

        // Material 필드 세팅
        values.stream()
            .filter(v -> v.getSelectableValue().getDefinition() != null && v.getSelectableValue().getDefinition().getName().equalsIgnoreCase("Material"))
            .findFirst()
            .ifPresent(v -> {
                try {
                    Material material = Material.valueOf(v.getSelectableValue().getValue().toUpperCase());
                    builder.material(material);
                } catch (IllegalArgumentException ignored) {}
            });

        return builder;
    }

}
