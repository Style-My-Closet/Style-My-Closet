package com.stylemycloset.weather.util;

import com.stylemycloset.weather.entity.Weather;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class DateTimeUtils {

    // 기상청 허용 base_time 목록 (시각만)
    private static final int[] ALLOWED_BASE_TIMES = {2, 5, 8, 11, 14, 17, 20, 23};


    public static List<String> toBaseDateAndTime(LocalDateTime now) {
        // baseDate는 그냥 오늘 날짜 (yyyyMMdd)
        String baseDate = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // 현재 시간 시(hour)만 추출
        int currentHour = now.getHour();

        // ALLOWED_BASE_TIMES에서 현재 시간 이하 중 가장 큰 값 찾기
        int baseHour = ALLOWED_BASE_TIMES[0];

        // 만약 현재 시간이 0~1시 사이면 전날 2300 사용해야 하므로
        if (currentHour < ALLOWED_BASE_TIMES[0]) {
            baseHour = ALLOWED_BASE_TIMES[ALLOWED_BASE_TIMES.length - 1]; // 23시
            baseDate = now.minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        }

        // baseTime은 HH00 형식 (예: 0200, 1100)
        String baseTime = String.format("%02d00", baseHour);

        return List.of(baseDate, baseTime);
    }

    public static String allowedBaseTime(String fcstTime) {
        int input = Integer.parseInt(fcstTime)/100;
        int closet;

        int idx = Arrays.binarySearch(ALLOWED_BASE_TIMES, input);
        if (idx >= 0) closet= ALLOWED_BASE_TIMES[idx]; // 정확히 일치하면 바로 반환

        // binarySearch가 음수 반환 시, -(insertion point) - 1
        idx = -idx - 1;

        if (idx == 0) closet = ALLOWED_BASE_TIMES[0];
        if (idx == ALLOWED_BASE_TIMES.length) closet= ALLOWED_BASE_TIMES[ALLOWED_BASE_TIMES.length - 1];

        int prev = ALLOWED_BASE_TIMES[idx - 1];
        int next = ALLOWED_BASE_TIMES[idx];

        closet= (Math.abs(input - prev) <= Math.abs(next - input)) ? prev : next;

        return String.format("%02d00", closet);
    }
}
