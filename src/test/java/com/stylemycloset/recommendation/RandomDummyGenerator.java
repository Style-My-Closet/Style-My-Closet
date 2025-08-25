package com.stylemycloset.recommendation;

import com.stylemycloset.recommendation.entity.ClothingCondition;
import com.stylemycloset.recommendation.entity.Color;
import com.stylemycloset.recommendation.entity.PantsLength;
import com.stylemycloset.recommendation.entity.SleeveLength;
import com.stylemycloset.user.entity.Gender;
import com.stylemycloset.weather.entity.Weather.AlertType;
import com.stylemycloset.weather.entity.Weather.SkyStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomDummyGenerator {

    private static final Random random = new Random();

    public static ClothingCondition generateRandomCondition() {
        ClothingCondition cc = ClothingCondition.builder()
            .temperature(15 + random.nextDouble() * 15) // 15~30도
            .windSpeed(random.nextDouble() * 10)    // 0~10 m/s
            .humidity(30 + random.nextDouble() * 70)   // 30~100%
            .gender(random.nextBoolean() ? Gender.MALE : Gender.FEMALE)
            .temperatureSensitivity(random.nextInt(3)) // 0,1,2
            .skyStatus(SkyStatus.values()[random.nextInt(SkyStatus.values().length)])
            .weatherType(AlertType.values()[random.nextInt(AlertType.values().length)])
            .color(Color.values()[random.nextInt(Color.values().length)])
            .sleeveLength(SleeveLength.values()[random.nextInt(SleeveLength.values().length)])
            .pantsLength(PantsLength.values()[random.nextInt(PantsLength.values().length)])
            .label(random.nextBoolean()) // 추천 여부
            .build();
        return cc;
    }

    public static List<ClothingCondition> generateDummyList(int n) {
        List<ClothingCondition> list = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            list.add(generateRandomCondition());
        }
        return list;
    }

}

