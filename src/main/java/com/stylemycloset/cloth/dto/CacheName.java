package com.stylemycloset.cloth.dto;

public enum CacheName {
    CLOTH_LIST_FIRST_PAGE("clothListFirstPage"),
    ATTRIBUTE_LIST_FIRST_PAGE("attributeListFirstPage");
    
    private final String value;
    
    CacheName(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
}
