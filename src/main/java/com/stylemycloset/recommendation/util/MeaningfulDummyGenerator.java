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

public class MeaningfulDummyGenerator {
    public static List<ClothingCondition> generateMeaningfulDummyList() {
        List<ClothingCondition> list = new ArrayList<>();

        // === 추천 데이터 10개 ===
        list.add(ClothingCondition.builder()
            .temperature(30).humidity(50).windSpeed(2)
            .gender(Gender.FEMALE).temperatureSensitivity(0)
            .skyStatus(SkyStatus.CLEAR).weatherType(AlertType.NONE)
            .color(Color.WHITE).length(Length.SHORT).material(Material.COTTON)
            .label(true).build());
        // 더운 날씨 + 반팔 + 통풍 좋은 면 → 추천

        list.add(ClothingCondition.builder()
            .temperature(5).humidity(40).windSpeed(3)
            .gender(Gender.MALE).temperatureSensitivity(-1)
            .skyStatus(SkyStatus.CLOUDY).weatherType(AlertType.NONE)
            .color(Color.BLACK).length(Length.LONG).material(Material.WOOL)
            .label(true).build());
        // 추운 날씨 + 보온성 좋은 울 코트 → 추천

        list.add(ClothingCondition.builder()
            .temperature(0).humidity(35).windSpeed(4)
            .gender(Gender.FEMALE).temperatureSensitivity(-2)
            .skyStatus(SkyStatus.CLEAR).weatherType(AlertType.SNOW)
            .color(Color.GRAY).length(Length.LONG).material(Material.DOWN)
            .label(true).build());
        // 한파 + 눈 + 다운 패딩 → 추천

        list.add(ClothingCondition.builder()
            .temperature(20).humidity(55).windSpeed(1)
            .gender(Gender.MALE).temperatureSensitivity(0)
            .skyStatus(SkyStatus.CLEAR).weatherType(AlertType.NONE)
            .color(Color.BLUE).length(Length.MEDIUM).material(Material.DENIM)
            .label(true).build());
        // 선선한 날씨 + 데님 자켓 → 추천

        list.add(ClothingCondition.builder()
            .temperature(15).humidity(45).windSpeed(2)
            .gender(Gender.FEMALE).temperatureSensitivity(1)
            .skyStatus(SkyStatus.CLOUDY).weatherType(AlertType.NONE)
            .color(Color.BEIGE).length(Length.MEDIUM).material(Material.CASHMERE)
            .label(true).build());
        // 쌀쌀한 날씨 + 캐시미어 니트 → 추천

        list.add(ClothingCondition.builder()
            .temperature(10).humidity(60).windSpeed(5)
            .gender(Gender.MALE).temperatureSensitivity(0)
            .skyStatus(SkyStatus.CLOUDY).weatherType(AlertType.NONE)
            .color(Color.NAVY).length(Length.LONG).material(Material.LEATHER)
            .label(true).build());
        // 쌀쌀 + 바람 많음 + 방풍되는 가죽자켓 → 추천

        list.add(ClothingCondition.builder()
            .temperature(25).humidity(70).windSpeed(3)
            .gender(Gender.FEMALE).temperatureSensitivity(0)
            .skyStatus(SkyStatus.CLOUDY).weatherType(AlertType.NONE)
            .color(Color.PINK).length(Length.SHORT).material(Material.LINEN)
            .label(true).build());
        // 덥고 습함 + 린넨 반팔 → 추천

        list.add(ClothingCondition.builder()
            .temperature(18).humidity(40).windSpeed(2)
            .gender(Gender.MALE).temperatureSensitivity(1)
            .skyStatus(SkyStatus.CLEAR).weatherType(AlertType.NONE)
            .color(Color.GREEN).length(Length.MEDIUM).material(Material.SYNTHETIC)
            .label(true).build());
        // 선선 + 활동적 날씨 + 기능성 합성 소재 → 추천

        list.add(ClothingCondition.builder()
            .temperature(28).humidity(65).windSpeed(2)
            .gender(Gender.FEMALE).temperatureSensitivity(0)
            .skyStatus(SkyStatus.CLEAR).weatherType(AlertType.NONE)
            .color(Color.YELLOW).length(Length.SHORT).material(Material.RAYON)
            .label(true).build());
        // 더움 + 통풍 잘 되는 레이온 원피스 → 추천

        list.add(ClothingCondition.builder()
            .temperature(8).humidity(55).windSpeed(4)
            .gender(Gender.MALE).temperatureSensitivity(-1)
            .skyStatus(SkyStatus.CLOUDY).weatherType(AlertType.NONE)
            .color(Color.BROWN).length(Length.LONG).material(Material.FLEECE)
            .label(true).build());
        // 추움 + 플리스 → 추천

        // === 비추천 데이터 10개 ===
        list.add(ClothingCondition.builder()
            .temperature(30).humidity(80).windSpeed(1)
            .gender(Gender.MALE).temperatureSensitivity(0)
            .skyStatus(SkyStatus.CLEAR).weatherType(AlertType.NONE)
            .color(Color.BLACK).length(Length.LONG).material(Material.WOOL)
            .label(false).build());
        // 한여름 + 울 코트 → 비추천

        list.add(ClothingCondition.builder()
            .temperature(5).humidity(30).windSpeed(4)
            .gender(Gender.FEMALE).temperatureSensitivity(-2)
            .skyStatus(SkyStatus.CLEAR).weatherType(AlertType.NONE)
            .color(Color.WHITE).length(Length.SHORT).material(Material.LINEN)
            .label(false).build());
        // 추운 날씨 + 린넨 반팔 → 비추천

        list.add(ClothingCondition.builder()
            .temperature(0).humidity(60).windSpeed(5)
            .gender(Gender.MALE).temperatureSensitivity(-1)
            .skyStatus(SkyStatus.MOSTLY_CLOUDY).weatherType(AlertType.SNOW)
            .color(Color.YELLOW).length(Length.SHORT).material(Material.COTTON)
            .label(false).build());
        // 한파 + 반팔 면티 → 비추천

        list.add(ClothingCondition.builder()
            .temperature(25).humidity(50).windSpeed(2)
            .gender(Gender.FEMALE).temperatureSensitivity(0)
            .skyStatus(SkyStatus.CLEAR).weatherType(AlertType.NONE)
            .color(Color.BLACK).length(Length.LONG).material(Material.LEATHER)
            .label(false).build());
        // 더운 날씨 + 가죽 롱코트 → 비추천

        list.add(ClothingCondition.builder()
            .temperature(15).humidity(70).windSpeed(3)
            .gender(Gender.MALE).temperatureSensitivity(1)
            .skyStatus(SkyStatus.CLOUDY).weatherType(AlertType.RAIN)
            .color(Color.WHITE).length(Length.SHORT).material(Material.COTTON)
            .label(false).build());
        // 비 오는 날 + 반팔 면티 (추위 탐) → 비추천

        list.add(ClothingCondition.builder()
            .temperature(35).humidity(40).windSpeed(1)
            .gender(Gender.FEMALE).temperatureSensitivity(0)
            .skyStatus(SkyStatus.CLEAR).weatherType(AlertType.NONE)
            .color(Color.GRAY).length(Length.LONG).material(Material.DENIM)
            .label(false).build());
        // 폭염 + 롱 데님 → 비추천

        list.add(ClothingCondition.builder()
            .temperature(10).humidity(30).windSpeed(6)
            .gender(Gender.MALE).temperatureSensitivity(-1)
            .skyStatus(SkyStatus.CLOUDY).weatherType(AlertType.NONE)
            .color(Color.YELLOW).length(Length.SHORT).material(Material.COTTON)
            .label(false).build());
        // 추움 + 반팔 면티 + 색상까지 부적합 → 비추천

        list.add(ClothingCondition.builder()
            .temperature(20).humidity(85).windSpeed(2)
            .gender(Gender.FEMALE).temperatureSensitivity(0)
            .skyStatus(SkyStatus.MOSTLY_CLOUDY).weatherType(AlertType.RAIN)
            .color(Color.PINK).length(Length.LONG).material(Material.WOOL)
            .label(false).build());
        // 습하고 더움 + 울 소재 → 비추천

        list.add(ClothingCondition.builder()
            .temperature(3).humidity(45).windSpeed(3)
            .gender(Gender.MALE).temperatureSensitivity(-2)
            .skyStatus(SkyStatus.CLOUDY).weatherType(AlertType.SNOW)
            .color(Color.BLUE).length(Length.SHORT).material(Material.SILK)
            .label(false).build());
        // 영하권 + 얇은 실크 반팔 → 비추천

        list.add(ClothingCondition.builder()
            .temperature(27).humidity(75).windSpeed(4)
            .gender(Gender.FEMALE).temperatureSensitivity(0)
            .skyStatus(SkyStatus.CLEAR).weatherType(AlertType.NONE)
            .color(Color.GREEN).length(Length.LONG).material(Material.WOOL)
            .label(false).build());
        // 더움 + 울 롱코트 → 비추천

        return list;
    }
}
