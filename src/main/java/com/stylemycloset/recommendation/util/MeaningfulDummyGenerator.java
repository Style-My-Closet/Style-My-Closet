package com.stylemycloset.recommendation.util;

import com.stylemycloset.recommendation.entity.ClothingCondition;
import com.stylemycloset.recommendation.entity.Length;
import com.stylemycloset.recommendation.entity.Material;
import com.stylemycloset.user.entity.Gender;
import com.stylemycloset.weather.entity.Weather.AlertType;
import com.stylemycloset.weather.entity.Weather.SkyStatus;
import com.stylemycloset.recommendation.entity.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MeaningfulDummyGenerator {
    public static List<ClothingCondition> generateMeaningfulDummyList() {
        List<ClothingCondition> list = new ArrayList<>();
        Random random = new Random();

        for (Material material : Material.values()) {
            for (Color color : Color.values()) {
                for (Length length : Length.values()) {

                    double temperature;
                    boolean label;

                    // 간단한 추천 규칙
                    if (isWinterMaterial(material)) {
                        temperature = random.nextInt(5); // 0~4도
                        label = (length != Length.SHORT); // 겨울 옷인데 SHORT면 비추천
                    } else if (isSummerMaterial(material)) {
                        temperature = 25 + random.nextInt(10); // 25~34도
                        label = (length != Length.LONG); // 여름 옷인데 LONG이면 비추천
                    } else {
                        temperature = 10 + random.nextInt(15); // 10~24도
                        label = true; // 중간 계절은 대체로 추천
                    }

                    // 날씨 조건 랜덤 배치
                    SkyStatus skyStatus = SkyStatus.values()[random.nextInt(SkyStatus.values().length)];
                    AlertType alertType = AlertType.values()[random.nextInt(AlertType.values().length)];
                    Gender gender = random.nextBoolean() ? Gender.MALE : Gender.FEMALE;

                    ClothingCondition cc = ClothingCondition.builder()
                        .temperature(temperature)
                        .humidity(30 + random.nextInt(50))  // 30~80%
                        .windSpeed(random.nextInt(6))       // 0~5 m/s
                        .gender(gender)
                        .temperatureSensitivity(random.nextInt(3) - 1) // -1 ~ 1
                        .skyStatus(skyStatus)
                        .weatherType(alertType)
                        .color(color)
                        .length(length)
                        .material(material)
                        .label(label)
                        .build();

                    list.add(cc);
                }
            }
        }

        return list;
    }

    private static boolean isWinterMaterial(Material material) {
        return material == Material.WOOL || material == Material.CASHMERE ||
            material == Material.FLEECE || material == Material.DOWN ||
            material == Material.LEATHER;
    }

    private static boolean isSummerMaterial(Material material) {
        return material == Material.COTTON || material == Material.LINEN ||
            material == Material.RAYON || material == Material.SILK;
    }
}
