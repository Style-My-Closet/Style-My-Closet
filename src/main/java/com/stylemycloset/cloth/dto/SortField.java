package com.stylemycloset.cloth.dto;

public enum SortField {
    CREATED_AT("createdAt"),
    NAME("name");
    
    private final String value;
    
    SortField(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public static SortField fromString(String value) {
        for (SortField field : values()) {
            if (field.value.equalsIgnoreCase(value)) {
                return field;
            }
        }
        throw new IllegalArgumentException("Unknown sort field: " + value);
    }
}
