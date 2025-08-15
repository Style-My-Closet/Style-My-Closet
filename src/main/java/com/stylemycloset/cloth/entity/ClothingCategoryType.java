package com.stylemycloset.cloth.entity;

import lombok.Getter;

@Getter
public enum ClothingCategoryType {
        TOP("상의"),
        BOTTOM("하의"), 
        DRESS("원피스"),
        OUTER("아우터"),
        UNDERWEAR("속옷"),
        ACCESSORY("액세서리", "악세사리"), // 흔한 오타 포함
        SHOES("신발"),
        SOCKS("양말"),
        HAT("모자"),
        BAG("가방", "가빙"), // 오타 포함
        SCARF("스카프"),
        OTHER("기타");
        
        private final String description;
        private final String[] aliases; // 오타나 동의어 포함

        ClothingCategoryType(String description, String... aliases) {
            this.description = description;
            this.aliases = aliases;
        }

    public static ClothingCategoryType from(String value) {
        if (value == null || value.trim().isEmpty()) {
            return OTHER;
        }
        
        String trimmedValue = value.trim();
        
        // 1) Enum 상수명으로 매칭 시도
        try {
            return ClothingCategoryType.valueOf(trimmedValue.toUpperCase());
        } catch (Exception ignore) {
        }
        
        // 2) 한글 설명 및 오타/동의어 매칭
        for (ClothingCategoryType type : values()) {
            // 기본 설명과 매칭
            if (type.description.equalsIgnoreCase(trimmedValue)) {
                return type;
            }
            
            // 오타/동의어와 매칭
            if (type.aliases != null) {
                for (String alias : type.aliases) {
                    if (alias.equalsIgnoreCase(trimmedValue)) {
                        return type;
                    }
                }
            }
        }
        
        return OTHER;
    }
}

