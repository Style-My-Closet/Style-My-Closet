package com.stylemycloset.cloth.entity;

import lombok.Getter;

@Getter
public enum ClothingCategoryType {
        TOP("상의"),
        BOTTOM("하의"),
        DRESS("원피스"),
        OUTER("아우터"),
        UNDERWEAR("속옷"),
        ACCESSORY("액세서리"),
        SHOES("신발"),
        SOCKS("양말"),
        HAT("모자"),
        BAG("가방"),
        SCARF("스카프"),
        OTHER("기타");
         //추가 예정
        private final String description;

        ClothingCategoryType(String description) {
            this.description = description;
        }

    public static ClothingCategoryType from(String value) {
            if (value == null || value.trim().isEmpty()) {
                return OTHER;
            }
            try {
                return ClothingCategoryType.valueOf(value.trim().toUpperCase());
            } catch (Exception ignore) {
            }
            //한글 설명과 매칭 시도
            for (ClothingCategoryType type : values()) {
                if (type.description.equalsIgnoreCase(value.trim())) {
                    return type;
                }
            }
            // 3) 동의어/오타 보정
            String v = value.trim();
            if ("악세사리".equalsIgnoreCase(v)) return ACCESSORY; // 흔한 표기
            if ("액세서리".equalsIgnoreCase(v)) return ACCESSORY;
            if ("상의".equalsIgnoreCase(v)) return TOP;
            if ("하의".equalsIgnoreCase(v)) return BOTTOM;
            if ("원피스".equalsIgnoreCase(v)) return DRESS;
            if ("아우터".equalsIgnoreCase(v)) return OUTER;
            if ("속옷".equalsIgnoreCase(v)) return UNDERWEAR;
            if ("신발".equalsIgnoreCase(v)) return SHOES;
            if ("양말".equalsIgnoreCase(v)) return SOCKS;
            if ("모자".equalsIgnoreCase(v)) return HAT;
            if ("가방".equalsIgnoreCase(v)) return BAG;
            if ("스카프".equalsIgnoreCase(v)) return SCARF;
            if ("기타".equalsIgnoreCase(v)) return OTHER;
            return OTHER;
        }
}

