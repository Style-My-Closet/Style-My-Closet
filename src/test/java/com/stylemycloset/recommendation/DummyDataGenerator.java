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

public class DummyDataGenerator {

    private static final Random random = new Random();

    public static ClothingCondition generateRandomCondition() {
        ClothingCondition cc = new ClothingCondition();
        cc.setTemperature(15 + random.nextDouble() * 15); // 15~30도
        cc.setWindSpeed(random.nextDouble() * 10);       // 0~10 m/s
        cc.setHumidity(30 + random.nextDouble() * 70);   // 30~100%
        cc.setGender(random.nextBoolean() ? Gender.MALE : Gender.FEMALE);
        cc.setTemperatureSensitivity(random.nextInt(3)); // 0,1,2
        cc.setSkyStatus(SkyStatus.values()[random.nextInt(SkyStatus.values().length)]);
        cc.setWeatherType(AlertType.values()[random.nextInt(AlertType.values().length)]);
        cc.setColor(Color.values()[random.nextInt(Color.values().length)]);
        cc.setSleeveLength(SleeveLength.values()[random.nextInt(SleeveLength.values().length)]);
        cc.setPantsLength(PantsLength.values()[random.nextInt(PantsLength.values().length)]);
        cc.setLabel(random.nextBoolean()); // 추천 여부
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

