package com.stylemycloset.recommendation.util;

import com.stylemycloset.recommendation.entity.ClothingCondition;
import com.stylemycloset.recommendation.entity.PantsLength;
import com.stylemycloset.recommendation.entity.SleeveLength;
import com.stylemycloset.user.entity.Gender;
import com.stylemycloset.weather.entity.Weather.AlertType;
import com.stylemycloset.weather.entity.Weather.SkyStatus;
import com.stylemycloset.recommendation.entity.Color;
import java.util.ArrayList;
import java.util.List;

public class MeaningfulDummyGenerator {
    public static List<ClothingCondition> generateMeaningfulDummyList() {
        List<ClothingCondition> list = new ArrayList<>();

        // 1. 여름 맑은 날, 반팔, 반바지, 추천
        list.add(ClothingCondition.builder()
            .temperature(32)
            .windSpeed(2)
            .humidity(50)
            .skyStatus(SkyStatus.CLEAR)
            .weatherType(AlertType.NONE)
            .gender(Gender.MALE)
            .temperatureSensitivity(3)
            .color(Color.YELLOW) // 가정: 맑은 날 밝은 색 추천
            .sleeveLength(SleeveLength.SHORT_SLEEVE)
            .pantsLength(PantsLength.SHORT_PANTS)
            .label(true)
            .build());

        // 2. 여름 흐린 날, 반팔, 반바지, 추천
        list.add(ClothingCondition.builder()
            .temperature(30)
            .windSpeed(1)
            .humidity(60)
            .skyStatus(SkyStatus.CLOUDY)
            .weatherType(AlertType.NONE)
            .gender(Gender.FEMALE)
            .temperatureSensitivity(2)
            .color(Color.BLUE)
            .sleeveLength(SleeveLength.SHORT_SLEEVE)
            .pantsLength(PantsLength.SHORT_PANTS)
            .label(true)
            .build());

        // 3. 장마철 비오는 날, 긴팔, 긴바지, 추천
        list.add(ClothingCondition.builder()
            .temperature(24)
            .windSpeed(3)
            .humidity(80)
            .skyStatus(SkyStatus.CLOUDY)
            .weatherType(AlertType.RAIN)
            .gender(Gender.MALE)
            .temperatureSensitivity(3)
            .color(Color.BLACK)
            .sleeveLength(SleeveLength.LONG_SLEEVE)
            .pantsLength(PantsLength.LONG_PANTS)
            .label(true)
            .build());

        // 4. 겨울 맑은 날, 긴팔, 긴바지, 추천
        list.add(ClothingCondition.builder()
            .temperature(5)
            .windSpeed(5)
            .humidity(40)
            .skyStatus(SkyStatus.CLEAR)
            .weatherType(AlertType.LOW_TEMP)
            .gender(Gender.FEMALE)
            .temperatureSensitivity(1)
            .color(Color.WHITE)
            .sleeveLength(SleeveLength.LONG_SLEEVE)
            .pantsLength(PantsLength.LONG_PANTS)
            .label(true)
            .build());

        // 5. 겨울 눈 오는 날, 긴팔, 긴바지, 추천
        list.add(ClothingCondition.builder()
            .temperature(-2)
            .windSpeed(4)
            .humidity(70)
            .skyStatus(SkyStatus.CLOUDY)
            .weatherType(AlertType.SNOW)
            .gender(Gender.MALE)
            .temperatureSensitivity(1)
            .color(Color.BLACK)
            .sleeveLength(SleeveLength.LONG_SLEEVE)
            .pantsLength(PantsLength.LONG_PANTS)
            .label(true)
            .build());

        // 6. 봄 맑은 날, 반팔, 긴바지, 추천
        list.add(ClothingCondition.builder()
            .temperature(18)
            .windSpeed(2)
            .humidity(50)
            .skyStatus(SkyStatus.CLEAR)
            .weatherType(AlertType.NONE)
            .gender(Gender.FEMALE)
            .temperatureSensitivity(2)
            .color(Color.RED)
            .sleeveLength(SleeveLength.SHORT_SLEEVE)
            .pantsLength(PantsLength.LONG_PANTS)
            .label(true)
            .build());

        // 7. 봄 비오는 날, 긴팔, 긴바지, 추천
        list.add(ClothingCondition.builder()
            .temperature(15)
            .windSpeed(3)
            .humidity(75)
            .skyStatus(SkyStatus.CLOUDY)
            .weatherType(AlertType.SHOWER)
            .gender(Gender.MALE)
            .temperatureSensitivity(2)
            .color(Color.BLUE)
            .sleeveLength(SleeveLength.LONG_SLEEVE)
            .pantsLength(PantsLength.LONG_PANTS)
            .label(true)
            .build());

        // 8. 여름 고온, 반팔, 반바지, 추천
        list.add(ClothingCondition.builder()
            .temperature(35)
            .windSpeed(1)
            .humidity(40)
            .skyStatus(SkyStatus.CLEAR)
            .weatherType(AlertType.HIGH_TEMP)
            .gender(Gender.FEMALE)
            .temperatureSensitivity(3)
            .color(Color.YELLOW)
            .sleeveLength(SleeveLength.SHORT_SLEEVE)
            .pantsLength(PantsLength.SHORT_PANTS)
            .label(true)
            .build());

        // 9. 선선한 날, 긴팔, 반바지, 추천
        list.add(ClothingCondition.builder()
            .temperature(22)
            .windSpeed(2)
            .humidity(55)
            .skyStatus(SkyStatus.MOSTLY_CLOUDY)
            .weatherType(AlertType.NONE)
            .gender(Gender.MALE)
            .temperatureSensitivity(2)
            .color(Color.WHITE)
            .sleeveLength(SleeveLength.LONG_SLEEVE)
            .pantsLength(PantsLength.SHORT_PANTS)
            .label(true)
            .build());

        // 10. 장마철 습한 날, 긴팔, 긴바지, 추천
        list.add(ClothingCondition.builder()
            .temperature(20)
            .windSpeed(3)
            .humidity(85)
            .skyStatus(SkyStatus.CLOUDY)
            .weatherType(AlertType.HEAVY_RAIN)
            .gender(Gender.FEMALE)
            .temperatureSensitivity(3)
            .color(Color.BLUE)
            .sleeveLength(SleeveLength.LONG_SLEEVE)
            .pantsLength(PantsLength.LONG_PANTS)
            .label(true)
            .build());

        // 추가 6개 비추천 데이터
        list.add(ClothingCondition.builder()
            .temperature(35)
            .windSpeed(0)
            .humidity(80)
            .skyStatus(SkyStatus.CLOUDY)
            .weatherType(AlertType.HIGH_TEMP)
            .gender(Gender.MALE)
            .temperatureSensitivity(3)
            .color(Color.BLACK)
            .sleeveLength(SleeveLength.LONG_SLEEVE)
            .pantsLength(PantsLength.LONG_PANTS)
            .label(false)
            .build());

        list.add(ClothingCondition.builder()
            .temperature(10)
            .windSpeed(5)
            .humidity(60)
            .skyStatus(SkyStatus.CLOUDY)
            .weatherType(AlertType.LOW_TEMP)
            .gender(Gender.FEMALE)
            .temperatureSensitivity(1)
            .color(Color.BLUE)
            .sleeveLength(SleeveLength.SHORT_SLEEVE)
            .pantsLength(PantsLength.SHORT_PANTS)
            .label(false)
            .build());

        list.add(ClothingCondition.builder()
            .temperature(20)
            .windSpeed(4)
            .humidity(90)
            .skyStatus(SkyStatus.MOSTLY_CLOUDY)
            .weatherType(AlertType.RAIN)
            .gender(Gender.MALE)
            .temperatureSensitivity(2)
            .color(Color.WHITE)
            .sleeveLength(SleeveLength.SHORT_SLEEVE)
            .pantsLength(PantsLength.SHORT_PANTS)
            .label(false)
            .build());

        list.add(ClothingCondition.builder()
            .temperature(30)
            .windSpeed(3)
            .humidity(85)
            .skyStatus(SkyStatus.CLOUDY)
            .weatherType(AlertType.HEAVY_RAIN)
            .gender(Gender.FEMALE)
            .temperatureSensitivity(3)
            .color(Color.RED)
            .sleeveLength(SleeveLength.SHORT_SLEEVE)
            .pantsLength(PantsLength.SHORT_PANTS)
            .label(false)
            .build());

        list.add(ClothingCondition.builder()
            .temperature(15)
            .windSpeed(6)
            .humidity(70)
            .skyStatus(SkyStatus.CLEAR)
            .weatherType(AlertType.STRONG_WIND)
            .gender(Gender.MALE)
            .temperatureSensitivity(1)
            .color(Color.WHITE)
            .sleeveLength(SleeveLength.LONG_SLEEVE)
            .pantsLength(PantsLength.LONG_PANTS)
            .label(false)
            .build());

        list.add(ClothingCondition.builder()
            .temperature(5)
            .windSpeed(2)
            .humidity(95)
            .skyStatus(SkyStatus.CLOUDY)
            .weatherType(AlertType.SNOW)
            .gender(Gender.FEMALE)
            .temperatureSensitivity(2)
            .color(Color.BLACK)
            .sleeveLength(SleeveLength.SHORT_SLEEVE)
            .pantsLength(PantsLength.SHORT_PANTS)
            .label(false)
            .build());


        return list;
    }
}
