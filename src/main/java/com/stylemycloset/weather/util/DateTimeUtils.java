package com.stylemycloset.weather.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DateTimeUtils {

    public static List<String> toBaseDateAndTime(LocalDateTime dateTime) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HHmm");

        String baseDate = dateTime.format(dateFormatter);
        String baseTime = dateTime.format(timeFormatter);

        List<String> result = new ArrayList<>();
        result.add( baseDate);
        result.add( baseTime);

        return result;
    }
}