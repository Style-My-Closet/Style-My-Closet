package com.stylemycloset.cloth.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class RawSiteData {
    
    private final Map<String, String> extractedFields;
    private final List<String> extractedImages;
    private final String sourceUrl;
    private final String siteName;
    
    public static RawSiteData empty(String sourceUrl, String siteName) {
        return RawSiteData.builder()
                .extractedFields(new HashMap<>())
                .extractedImages(List.of())
                .sourceUrl(sourceUrl)
                .siteName(siteName)
                .build();
    }
    
    public String getField(String key) {
        return extractedFields.get(key);
    }
    
    public String getFieldOrDefault(String key, String defaultValue) {
        return extractedFields.getOrDefault(key, defaultValue);
    }
    
    public boolean hasField(String key) {
        return extractedFields.containsKey(key) && 
               extractedFields.get(key) != null && 
               !extractedFields.get(key).trim().isEmpty();
    }
}